package akio.apps.myrun.feature.routetracking.impl

import akio.apps.myrun.R
import akio.apps.myrun._base.utils.StatsPresentations
import akio.apps.myrun._base.utils.flowTimer
import akio.apps.myrun._base.utils.toGmsLatLng
import akio.apps.myrun.data.authentication.UserAuthenticationState
import akio.apps.myrun.data.fitness.FitnessDataRepository
import akio.apps.myrun.data.location.LocationDataSource
import akio.apps.myrun.data.location.LocationEntity
import akio.apps.myrun.data.location.LocationRequestEntity
import akio.apps.myrun.data.routetracking.RouteTrackingLocationRepository
import akio.apps.myrun.data.routetracking.RouteTrackingState
import akio.apps.myrun.data.routetracking.RouteTrackingStatus
import akio.apps.myrun.feature.routetracking.ClearRouteTrackingStateUsecase
import android.annotation.SuppressLint
import android.app.ActivityManager
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.location.Location
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationRequest
import com.google.maps.android.SphericalUtil
import dagger.android.AndroidInjection
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import timber.log.Timber
import javax.inject.Inject

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

    private val exceptionHandler = CoroutineExceptionHandler { _, exception ->
        Timber.e(exception)
    }

    private val mainScope = CoroutineScope(SupervisorJob() + Dispatchers.Main + exceptionHandler)

    private var locationUpdateJob: Job? = null
    private var trackingTimerJob: Job? = null
    private var startLocation: Location? = null

    private val instantSpeedCalculator = InstantSpeedCalculator()
    private val routeDistanceCalculator = RouteDistanceCalculator()

    private var wakeLock: PowerManager.WakeLock? = null

    override fun onCreate() {
        super.onCreate()

        AndroidInjection.inject(this)

        createNotificationChannel()

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
    private fun requestLocationUpdates() {
        val locationRequest = createLocationTrackingRequest()

        locationUpdateJob?.cancel()
        locationUpdateJob = locationDataSource.getLocationUpdate(
            LocationRequestEntity(
                locationRequest.fastestInterval,
                locationRequest.interval,
                locationRequest.maxWaitTime,
                locationRequest.priority,
                locationRequest.smallestDisplacement
            )
        )
            .onEach(::onLocationUpdate)
            .launchIn(mainScope)
    }

    private suspend fun onLocationUpdate(locations: List<Location>) {
        routeTrackingLocationRepository.insert(locations)

        routeDistanceCalculator.calculateRouteDistance(locations)
        instantSpeedCalculator.calculateInstantSpeed()

        if (startLocation == null) {
            startLocation = locations.firstOrNull()
                ?.also {
                    routeTrackingState.setStartLocation(
                        LocationEntity(
                            it.latitude,
                            it.longitude,
                            it.altitude
                        )
                    )
                }
        }
    }

    inner class InstantSpeedCalculator() {
        private var lastTimeComputingInstantSpeed = 0L
        private var lastDistanceComputingInstantSpeed = 0.0

        suspend fun calculateInstantSpeed() {
            val routeDistance: Double = routeTrackingState.getRouteDistance()
            val currentTime = System.currentTimeMillis()
            val deltaTime = currentTime - lastTimeComputingInstantSpeed
            // compute instant speed for a period equals to location update interval
            if (lastTimeComputingInstantSpeed == 0L || (lastTimeComputingInstantSpeed > 0 && deltaTime >= LOCATION_UPDATE_INTERVAL)) {
                val instantSpeed = (routeDistance - lastDistanceComputingInstantSpeed) / deltaTime
                routeTrackingState.setInstantSpeed(instantSpeed)

                lastTimeComputingInstantSpeed = currentTime
                lastDistanceComputingInstantSpeed = routeDistance
            }
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
        val pendingIntent = PendingIntent.getActivity(
            this@RouteTrackingService,
            0,
            notificationIntent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )

        val durationDisplay =
            StatsPresentations.getDisplayDuration(routeTrackingState.getTrackingDuration())
        val distanceDisplay =
            StatsPresentations.getDisplayTrackingDistance(routeTrackingState.getRouteDistance())
        val notification: Notification =
            NotificationCompat.Builder(this@RouteTrackingService, NOTIF_CHANNEL_ID)
                .setContentTitle(getString(R.string.route_tracking_notification_title))
                .setContentText("$durationDisplay - $distanceDisplay Km")
                .setSmallIcon(R.drawable.ic_run_circle)
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
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
        mainScope.launch {
            val startTime = System.currentTimeMillis()
            routeTrackingState.setTrackingStatus(RouteTrackingStatus.RESUMED)
            routeTrackingState.setTrackingStartTime(startTime)
            routeTrackingState.setLastResumeTime(startTime)
        }

        notifyTrackingNotification()
        requestLocationUpdates()
        fitnessDataRepository.subscribeFitnessData()
        startTrackingTimer()
        acquireWakeLock()
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

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                NOTIF_CHANNEL_ID,
                "Route Tracking",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            channel.description = "While app is tracking your route"
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
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
        const val NOTIF_ID_TRACKING = 101

        const val NOTIF_CHANNEL_ID = "NOTIF_CHANNEL_ID"

        const val LOCATION_UPDATE_INTERVAL = 2000L
        const val LOCATION_MAX_WAIT_TIME = 8000L
        const val SMALLEST_DISPLACEMENT = 1f

        const val TRACKING_TIMER_PERIOD = 1000L

        const val ACTION_START = "ACTION_START"
        const val ACTION_STOP = "ACTION_STOP"
        const val ACTION_RESUME = "ACTION_RESUME"
        const val ACTION_PAUSE = "ACTION_PAUSE"

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

        fun createLocationTrackingRequest() = LocationRequest().apply {
            fastestInterval = LOCATION_UPDATE_INTERVAL
            interval = LOCATION_UPDATE_INTERVAL
            maxWaitTime = LOCATION_MAX_WAIT_TIME
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            smallestDisplacement = SMALLEST_DISPLACEMENT
        }

        @Suppress("DEPRECATION")
        fun isTrackingServiceRunning(context: Context): Boolean {
            val activityMan = ContextCompat.getSystemService(context, ActivityManager::class.java)
            val infos = activityMan?.getRunningServices(Int.MAX_VALUE)
            return infos?.any { RouteTrackingService::class.java.name == it.service.className } == true
        }
    }
}
