package akio.apps.myrun.feature.routetracking.impl

import akio.apps.myrun.R
import akio.apps.myrun._base.notification.AppNotificationChannel
import akio.apps.myrun._base.utils.StatsPresentations
import akio.apps.myrun._base.utils.flowTimer
import akio.apps.myrun._base.utils.toGmsLatLng
import akio.apps.myrun.data.activity.model.ActivityType
import akio.apps.myrun.data.authentication.UserAuthenticationState
import akio.apps.myrun.data.fitness.FitnessDataRepository
import akio.apps.myrun.data.location.LocationDataSource
import akio.apps.myrun.data.location.LocationEntity
import akio.apps.myrun.data.routetracking.RouteTrackingConfiguration
import akio.apps.myrun.data.routetracking.RouteTrackingLocationRepository
import akio.apps.myrun.data.routetracking.RouteTrackingState
import akio.apps.myrun.data.routetracking.RouteTrackingStatus
import akio.apps.myrun.data.routetracking.model.LocationProcessingConfig
import akio.apps.myrun.data.routetracking.model.LocationRequestConfig
import akio.apps.myrun.data.time.Now
import akio.apps.myrun.domain.routetracking.AverageLocationAccumulator
import akio.apps.myrun.domain.routetracking.ClearRouteTrackingStateUsecase
import akio.apps.myrun.domain.routetracking.LocationProcessorContainer
import akio.apps.myrun.domain.routetracking.LocationSpeedFilter
import akio.apps.myrun.feature.routetracking._di.DaggerRouteTrackingFeatureComponent
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
import com.google.maps.android.SphericalUtil
import javax.inject.Inject
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
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

    private var locationProcessors: LocationProcessorContainer = LocationProcessorContainer()

    private val exceptionHandler = CoroutineExceptionHandler { _, exception ->
        Timber.e(exception)
    }

    private val mainScope = CoroutineScope(SupervisorJob() + Dispatchers.Main + exceptionHandler)

    private var locationUpdateJob: Job? = null
    private var trackingTimerJob: Job? = null
    private var startLocation: LocationEntity? = null

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
                    clearRouteTrackingStateUsecase.clear()
                }
            }
            .launchIn(mainScope)
    }

    @SuppressLint("MissingPermission")
    private fun requestLocationUpdates() = mainScope.launch {
        val locationRequest = routeTrackingConfiguration.getLocationRequestConfig().first()
        val locationProcessingConfig =
            routeTrackingConfiguration.getLocationProcessingConfig().first()
        configureLocationProcessors(locationRequest, locationProcessingConfig)

        locationUpdateJob?.cancel()
        locationUpdateJob = locationDataSource.getLocationUpdate(locationRequest)
            .onEach(::onLocationUpdate)
            .launchIn(mainScope)
    }

    private suspend fun configureLocationProcessors(
        locationRequest: LocationRequestConfig,
        locationProcessingConfig: LocationProcessingConfig
    ) {
        Timber.d("requestLocationUpdates with processors")
        if (locationProcessingConfig.isSpeedFilterEnabled) {
            val maxValidSpeed = when (routeTrackingState.getActivityType()) {
                ActivityType.Running -> LocationSpeedFilter.RUNNING_MAX_SPEED
                ActivityType.Cycling -> LocationSpeedFilter.CYCLING_MAX_SPEED
                else -> Double.MAX_VALUE
            }
            Timber.d("add speed filter, maxValidSpeed=$maxValidSpeed")
            locationProcessors.addProcessor(LocationSpeedFilter(maxValidSpeed))
        }

        if (locationProcessingConfig.isAvgAccumulatorEnabled) {
            Timber.d("avg location accumulator, interval=${locationRequest.updateInterval}")
            locationProcessors.addProcessor(
                AverageLocationAccumulator(locationRequest.updateInterval, Now())
            )
        }
    }

    private suspend fun onLocationUpdate(locations: List<LocationEntity>) {
        Timber.d("onLocationUpdate ${locations.size}")
        val batch: List<LocationEntity> = locationProcessors.process(locations)
        if (batch.isEmpty()) {
            return
        }
        Timber.d("onLocationUpdate to be processed ${batch.size}")
        routeTrackingLocationRepository.insert(locations)

        routeDistanceCalculator.calculateRouteDistance(locations)
        routeTrackingState.setInstantSpeed(locations.last().speed)

        if (startLocation == null) {
            val firstLocation = locations.firstOrNull()
            if (firstLocation != null) {
                routeTrackingState.setStartLocation(firstLocation)
                startLocation = firstLocation
            }
        }
    }

    inner class RouteDistanceCalculator {
        private var lastComputeLengthLocation: LocationEntity? = null

        suspend fun calculateRouteDistance(locations: List<LocationEntity>): Double {
            val computeLengthLocations = mutableListOf<LocationEntity>()

            // add last location of previous batch to compute distance to first location of this batch
            lastComputeLengthLocation?.let {
                computeLengthLocations.add(it)
            }
            computeLengthLocations.addAll(locations)

            val locationUpdateDistance =
                SphericalUtil.computeLength(computeLengthLocations.map { it.toGmsLatLng() })

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
            StatsPresentations.getDisplayDuration(routeTrackingState.getTrackingDuration())
        val distanceDisplay =
            StatsPresentations.getDisplayTrackingDistance(routeTrackingState.getRouteDistance())
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
            .build()

        startForeground(NOTIF_ID_TRACKING, notification)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent == null) {
            mainScope.launch {
                // service restarted after process is killed
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
        setRouteTrackingStartState()
        startTrackingTimer()
        notifyTrackingNotification()
        requestLocationUpdates()
        fitnessDataRepository.subscribeFitnessData()
        acquireWakeLock()
    }

    private fun setRouteTrackingStartState() = mainScope.launch {
        val startTime = System.currentTimeMillis()
        routeTrackingState.setTrackingStatus(RouteTrackingStatus.RESUMED)
        routeTrackingState.setTrackingStartTime(startTime)
        routeTrackingState.setLastResumeTime(startTime)
    }

    private fun startTrackingTimer() {
        trackingTimerJob?.cancel()
        trackingTimerJob = mainScope.flowTimer(TRACKING_TIMER_PERIOD, TRACKING_TIMER_PERIOD) {
            val trackingDuration = TRACKING_TIMER_PERIOD + routeTrackingState.getTrackingDuration()
            routeTrackingState.setTrackingDuration(trackingDuration)

            notifyTrackingNotification()
        }
    }

    private fun onActionPause() {
        mainScope.launch {
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
            routeTrackingState.setLastResumeTime(System.currentTimeMillis())
        }
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

        private const val INSTANT_SPEED_PERIOD = 5000L

        fun startIntent(
            context: Context
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
