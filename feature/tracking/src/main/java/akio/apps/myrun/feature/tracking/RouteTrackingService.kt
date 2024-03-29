package akio.apps.myrun.feature.tracking

import akio.apps.myrun.data.activity.api.model.ActivityLocation
import akio.apps.myrun.data.authentication.api.UserAuthenticationState
import akio.apps.myrun.data.location.api.LOG_TAG_LOCATION
import akio.apps.myrun.data.location.api.LocationDataSource
import akio.apps.myrun.data.location.api.model.Location
import akio.apps.myrun.data.location.api.model.LocationRequestConfig
import akio.apps.myrun.data.tracking.api.FitnessDataRepository
import akio.apps.myrun.data.tracking.api.RouteTrackingConfiguration
import akio.apps.myrun.data.tracking.api.RouteTrackingLocationRepository
import akio.apps.myrun.data.tracking.api.RouteTrackingState
import akio.apps.myrun.data.tracking.api.model.LocationProcessingConfig
import akio.apps.myrun.data.tracking.api.model.RouteTrackingStatus
import akio.apps.myrun.data.user.api.CurrentUserPreferences
import akio.apps.myrun.domain.tracking.ClearRouteTrackingStateUsecase
import akio.apps.myrun.domain.tracking.locationprocessor.AverageLocationAccumulator
import akio.apps.myrun.domain.tracking.locationprocessor.LocationProcessorComposite
import akio.apps.myrun.domain.tracking.locationprocessor.LocationSpeedFilter
import akio.apps.myrun.feature.core.AppNotificationChannel
import akio.apps.myrun.feature.core.flowTimer
import akio.apps.myrun.feature.core.measurement.UnitFormatterSetFactory
import akio.apps.myrun.feature.tracking.di.DaggerRouteTrackingFeatureComponent
import akio.apps.myrun.worker.UpdateUserRecentPlaceWorker
import android.annotation.SuppressLint
import android.app.ActivityManager
import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import androidx.core.content.ContextCompat
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.SphericalUtil
import javax.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber

class RouteTrackingService : Service() {

    @Inject
    lateinit var locationDataSource: LocationDataSource

    @Inject
    lateinit var routeTrackingLocationRepository: RouteTrackingLocationRepository

    @Inject
    lateinit var routeTrackingState: RouteTrackingState

    @Inject
    lateinit var userAuthenticationState: UserAuthenticationState

    @Inject
    lateinit var clearRouteTrackingStateUsecase: ClearRouteTrackingStateUsecase

    @Inject
    lateinit var fitnessDataRepository: FitnessDataRepository

    @Inject
    lateinit var routeTrackingConfiguration: RouteTrackingConfiguration

    @Inject
    lateinit var currentUserPreferences: CurrentUserPreferences

    private val exceptionHandler = CoroutineExceptionHandler { _, exception ->
        Timber.e(exception)
    }

    private val mainScope = CoroutineScope(SupervisorJob() + Dispatchers.Main + exceptionHandler)

    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO

    private var locationUpdateJob: Job? = null
    private var trackingTimerJob: Job? = null

    // Cached values to avoid getting from storage in event listeners
    private var cachedStartLocation: Location? = null

    /**
     * Store a version of activity start time to avoid missing value in concurrent accesses.
     */
    private var cachedStartTime: Long = 0

    private val routeDistanceCalculator = RouteDistanceCalculator()

    private var wakeLock: PowerManager.WakeLock? = null

    override fun onCreate() {
        DaggerRouteTrackingFeatureComponent.factory().create(application).inject(this)
        super.onCreate()
        AppNotificationChannel.TrackingService.createChannelCompat(this)
        trackUserAuthenticationState()
    }

    /**
     * Track to stop service if user is logged out
     */
    private fun trackUserAuthenticationState() {
        userAuthenticationState.getUserAccountFlow()
            .onEach {
                if (it == null) {
                    onActionStop()
                    CoroutineScope(ioDispatcher).launch {
                        clearRouteTrackingStateUsecase.clear()
                    }
                }
            }
            .launchIn(mainScope)
    }

    @SuppressLint("MissingPermission")
    private fun requestLocationUpdates() = mainScope.launch {
        val locationRequest = routeTrackingConfiguration.getLocationRequestConfig()
        val locationProcessingConfig =
            routeTrackingConfiguration.getLocationProcessingConfig().first()
        val processorComposite = createLocationProcessors(locationRequest, locationProcessingConfig)

        locationUpdateJob?.cancel()
        locationUpdateJob = locationDataSource.getLocationUpdate(locationRequest)
            .onEach { onLocationUpdate(it, processorComposite) }
            .launchIn(mainScope)
    }

    private suspend fun createLocationProcessors(
        locationRequest: LocationRequestConfig,
        locationProcessingConfig: LocationProcessingConfig,
    ): LocationProcessorComposite {
        Timber.d("requestLocationUpdates with processors")
        val processors = LocationProcessorComposite()
        if (locationProcessingConfig.isSpeedFilterEnabled) {
            val activityType = routeTrackingState.getActivityType()
            processors.addProcessor(LocationSpeedFilter.createInstance(activityType))
            Timber.d("add speed filter for $activityType")
        }

        if (locationProcessingConfig.isAvgAccumulatorEnabled) {
            Timber.d("avg location accumulator, interval=${locationRequest.updateInterval}")
            processors.addProcessor(AverageLocationAccumulator(locationRequest.maxUpdateInterval))
        }

        return processors
    }

    private suspend fun onLocationUpdate(
        locations: List<Location>,
        processorContainer: LocationProcessorComposite,
    ) = withContext(ioDispatcher) {
        Timber.tag(LOG_TAG_LOCATION).d("[RouteTrackingService] onLocationUpdate: ${locations.size}")
        val processedLocations: List<Location> = processorContainer.process(locations)
        if (processedLocations.isEmpty()) {
            return@withContext
        }
        routeTrackingLocationRepository.insert(
            mapLocationUpdateToActivityLocation(processedLocations)
        )

        routeDistanceCalculator.calculateRouteDistance(processedLocations)
        routeTrackingState.setInstantSpeed(processedLocations.last().speed)

        cacheStartLocation(processedLocations.first())
        routeTrackingState.setLastLocationUpdateTime(System.currentTimeMillis())
    }

    private suspend fun mapLocationUpdateToActivityLocation(
        locations: List<Location>,
    ): List<ActivityLocation> {
        val activityElapsedTime = getActivityElapsedTime()
        val firstLocationTime = locations.firstOrNull()?.elapsedTime
            ?: return emptyList()
        return locations.map { location ->
            ActivityLocation(
                activityElapsedTime + location.elapsedTime - firstLocationTime,
                location.latitude,
                location.longitude,
                location.altitude,
                location.speed
            )
        }
    }

    private suspend fun getActivityElapsedTime(): Long {
        val activityElapsedTime = System.currentTimeMillis() -
            getTrackingStartTime() -
            routeTrackingState.getPauseDuration()

        Timber.d("activityElapsedTime = $activityElapsedTime")

        return activityElapsedTime
    }

    private suspend fun getTrackingStartTime(): Long {
        cachedStartTime = routeTrackingState.getTrackingStartTime()
            ?: cachedStartTime
        return cachedStartTime
    }

    private suspend fun cacheStartLocation(firstOfBatch: Location) {
        if (cachedStartLocation == null) {
            cachedStartLocation = routeTrackingState.getStartLocation()
        }
        if (cachedStartLocation == null) {
            routeTrackingState.setStartLocation(firstOfBatch)
            cachedStartLocation = firstOfBatch
        }
    }

    inner class RouteDistanceCalculator {
        private var lastComputeLengthLocation: Location? = null

        suspend fun calculateRouteDistance(locations: List<Location>): Double {
            val computeLengthLocations = mutableListOf<Location>()

            // add last location of previous batch to compute distance to first location of this batch
            lastComputeLengthLocation?.let {
                computeLengthLocations.add(it)
            }
            computeLengthLocations.addAll(locations)

            val locationUpdateDistance = SphericalUtil.computeLength(
                computeLengthLocations.map { LatLng(it.latitude, it.longitude) }
            )

            // remember last location of this batch for later computing
            lastComputeLengthLocation = locations.lastOrNull()

            val routeDistance = routeTrackingState.getRouteDistance() + locationUpdateDistance
            routeTrackingState.setRouteDistance(routeDistance)

            return routeDistance
        }

        fun clearLastCalculatedLocation() {
            lastComputeLengthLocation = null
        }
    }

    private fun notifyTrackingNotification() = mainScope.launch {
        val preferredSystem = currentUserPreferences.getMeasureSystemFlow().first()
        val (distanceFormatter, _, _, durationFormatter) =
            UnitFormatterSetFactory.createUnitFormatterSet(preferredSystem)
        val notificationIntent = RouteTrackingActivity.launchIntent(this@RouteTrackingService)
        val intentFlag = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            PendingIntent.FLAG_UPDATE_CURRENT
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        }
        val pendingIntent = PendingIntent.getActivity(
            this@RouteTrackingService,
            0,
            notificationIntent,
            intentFlag
        )

        val durationDisplay =
            durationFormatter.getFormattedValue(routeTrackingState.getTrackingDuration())
        val distanceDisplay =
            distanceFormatter.getFormattedValue(routeTrackingState.getRouteDistance())
        val notificationBuilder: Notification.Builder =
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
                @Suppress("DEPRECATION")
                Notification.Builder(this@RouteTrackingService)
            } else {
                Notification.Builder(
                    this@RouteTrackingService,
                    AppNotificationChannel.TrackingService.id
                )
            }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            notificationBuilder.setForegroundServiceBehavior(
                Notification.FOREGROUND_SERVICE_IMMEDIATE
            )
        }
        val notification: Notification = notificationBuilder
            .setContentTitle(getString(R.string.route_tracking_notification_title))
            .setContentText("$durationDisplay - $distanceDisplay Km")
            .setSmallIcon(R.drawable.ic_run_circle)
            .setContentIntent(pendingIntent)
            .setOnlyAlertOnce(true)
            .setOngoing(true)
            .build()

        startForeground(NOTIF_ID_TRACKING, notification)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent == null) {
            mainScope.launch {
                // service restarts after process was killed
                if (routeTrackingState.getTrackingStatus() == RouteTrackingStatus.RESUMED) {
                    onActionResume(true)
                }
            }
        } else {
            when (intent.action) {
                ACTION_START -> onActionStart()
                ACTION_STOP -> onActionStop()
                ACTION_RESUME -> onActionResume(false)
                ACTION_PAUSE -> onActionPause()
            }
        }

        return START_STICKY
    }

    private fun onActionStart() {
        Timber.d("onActionStart")
        UpdateUserRecentPlaceWorker.enqueueImmediately(this)
        setRouteTrackingStartState(System.currentTimeMillis())
        startTrackingTimer()
        notifyTrackingNotification()
        requestLocationUpdates()
        fitnessDataRepository.subscribeFitnessData()
        acquireWakeLock()
    }

    private fun setRouteTrackingStartState(startTime: Long) {
        setTrackingStartTime(startTime)
        mainScope.launch {
            routeTrackingState.setTrackingStatus(RouteTrackingStatus.RESUMED)
        }
    }

    private fun setTrackingStartTime(startTime: Long) {
        cachedStartTime = startTime
        mainScope.launch {
            routeTrackingState.setTrackingStartTime(startTime)
        }
    }

    private fun startTrackingTimer() {
        trackingTimerJob?.cancel()
        trackingTimerJob = mainScope.flowTimer(TRACKING_TIMER_PERIOD, TRACKING_TIMER_PERIOD) {
            notifyTrackingNotification()

//            val trackingDuration = TRACKING_TIMER_PERIOD + routeTrackingState.getTrackingDuration()
            routeTrackingState.setTrackingDuration(getActivityElapsedTime())
        }
    }

    private fun onActionPause() {
        mainScope.launch {
            routeTrackingState.setLastPauseTime(System.currentTimeMillis())
            routeTrackingState.setTrackingStatus(RouteTrackingStatus.PAUSED)
        }

        locationUpdateJob?.cancel()
        trackingTimerJob?.cancel()

        // dont count the distance in paused moments
        routeDistanceCalculator.clearLastCalculatedLocation()

        fitnessDataRepository.unsubscribeFitnessData()
    }

    private fun onActionResume(isServiceRestart: Boolean) {
        Timber.d("onActionResume isServiceRestart=$isServiceRestart")

        requestLocationUpdates()
        fitnessDataRepository.subscribeFitnessData()
        startTrackingTimer()
        notifyTrackingNotification()

        mainScope.launch {
            routeTrackingState.setTrackingStatus(RouteTrackingStatus.RESUMED)
        }
        addPauseDuration(System.currentTimeMillis())
    }

    /**
     * Adds up to accumulated pause duration each time the tracking is resumed.
     */
    private fun addPauseDuration(resumeTime: Long) = mainScope.launch {
        val lastPauseTime = routeTrackingState.getLastPauseTime()
            ?: routeTrackingState.getLastLocationUpdateTime()
            ?: return@launch
        val currPauseDuration = routeTrackingState.getPauseDuration()
        val updatedPauseTime = currPauseDuration + resumeTime - lastPauseTime
        Timber.d("updatedPauseTime = $updatedPauseTime")
        routeTrackingState.setPauseDuration(updatedPauseTime)
        // pause duration already added up, this pause time is invalid so clear it here.
        routeTrackingState.setLastPauseTime(null)
    }

    private fun onActionStop() {
        Timber.d("onActionStop")

        mainScope.launch {
            routeTrackingState.setTrackingStatus(RouteTrackingStatus.STOPPED)
        }

        locationUpdateJob?.cancel()
        trackingTimerJob?.cancel()
        fitnessDataRepository.unsubscribeFitnessData()
        releaseWakeLock()
        stopSelf()
    }

    private fun acquireWakeLock() {
        wakeLock =
            (getSystemService(Context.POWER_SERVICE) as PowerManager).run {
                newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "RouteTrackingService::lock").apply {
                    acquire()
                }
            }
    }

    private fun releaseWakeLock() {
        wakeLock?.let {
            if (it.isHeld) {
                it.release()
            }
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        private val NOTIF_ID_TRACKING =
            AppNotificationChannel.TrackingService.nextNotificationStaticId()

        private const val TRACKING_TIMER_PERIOD = 1000L

        private const val ACTION_START = "ACTION_START"
        private const val ACTION_STOP = "ACTION_STOP"
        private const val ACTION_RESUME = "ACTION_RESUME"
        private const val ACTION_PAUSE = "ACTION_PAUSE"

        fun startIntent(
            context: Context,
        ): Intent {
            val intent = Intent(context, RouteTrackingService::class.java)
            intent.action = ACTION_START
            return intent
        }

        fun stopIntent(context: Context): Intent {
            val intent = Intent(context, RouteTrackingService::class.java)
            intent.action = ACTION_STOP
            return intent
        }

        fun pauseIntent(context: Context) =
            Intent(context, RouteTrackingService::class.java).apply {
                action = ACTION_PAUSE
            }

        fun resumeIntent(context: Context) =
            Intent(context, RouteTrackingService::class.java).apply {
                action = ACTION_RESUME
            }

        @Suppress("DEPRECATION")
        fun isTrackingServiceRunning(context: Context): Boolean {
            val activityMan = ContextCompat.getSystemService(context, ActivityManager::class.java)
            val infos = activityMan?.getRunningServices(Int.MAX_VALUE)
            return infos?.any {
                RouteTrackingService::class.java.name == it.service.className
            } == true
        }
    }
}
