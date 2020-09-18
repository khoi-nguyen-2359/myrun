package akio.apps.myrun.feature.routetracking.impl

import akio.apps.myrun.R
import akio.apps.myrun.data.location.LocationDataSource
import akio.apps.myrun.data.location.dto.LocationRequestEntity
import akio.apps.myrun.data.routetracking.RouteTrackingLocationRepository
import akio.apps.myrun.data.routetracking.RouteTrackingState
import akio.apps.myrun.feature._base.toGmsLatLng
import android.annotation.SuppressLint
import android.app.*
import android.content.Context
import android.content.Intent
import android.location.Location
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationRequest
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.maps.android.SphericalUtil
import dagger.android.AndroidInjection
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect
import timber.log.Timber
import javax.inject.Inject

class RouteTrackingService : Service() {

    private var locationUpdateJob: Job? = null

    private var wakeLock: PowerManager.WakeLock? = null

    @Inject
    lateinit var locationDataSource: LocationDataSource

    @Inject
    lateinit var routeTrackingLocationRepository: RouteTrackingLocationRepository

    @Inject
    lateinit var routeTrackingState: RouteTrackingState

    private val exceptionHandler = CoroutineExceptionHandler { context, exception ->
        exception.printStackTrace()
        FirebaseCrashlytics.getInstance().recordException(exception)
    }

    private val backgroundSupervisorJob = SupervisorJob()
    private val ioScope = CoroutineScope(backgroundSupervisorJob + Dispatchers.IO + exceptionHandler)
    private val mainScope = CoroutineScope(SupervisorJob() + Dispatchers.Main + exceptionHandler)

    override fun onCreate() {
        super.onCreate()

        AndroidInjection.inject(this)

        createNotificationChannel()
    }

    override fun onDestroy() {
        super.onDestroy()

        backgroundSupervisorJob.cancel()
    }

    @SuppressLint("MissingPermission")
    private fun requestLocationUpdates() {
        val locationRequest = createLocationTrackingRequest()

        val locationUpdateFlow = locationDataSource.getLocationUpdate(
            LocationRequestEntity(
                locationRequest.fastestInterval,
                locationRequest.interval,
                locationRequest.maxWaitTime,
                locationRequest.priority,
                locationRequest.smallestDisplacement
            )
        )

        locationUpdateJob?.cancel()
        locationUpdateJob = ioScope.launch {
            locationUpdateFlow.collect(::onLocationUpdate)
        }
    }

    var lastComputeLengthLocation: Location? = null
    private suspend fun onLocationUpdate(locations: List<Location>) {
        routeTrackingLocationRepository.insert(locations)

        computeRouteDistance(locations)

        computeInstantSpeed()
    }

    var lastTimeComputingInstantSpeed = 0L
    var lastDistanceComputingInstantSpeed = 0.0
    private suspend fun computeInstantSpeed() {
        val routeDistance: Double = routeTrackingState.getRouteDistance()
        val currentTime = System.currentTimeMillis()
        val deltaTime = currentTime - lastTimeComputingInstantSpeed
        if (lastTimeComputingInstantSpeed == 0L || (lastTimeComputingInstantSpeed > 0 && deltaTime >= LOCATION_UPDATE_INTERVAL)) {
            if (lastTimeComputingInstantSpeed > 0) {
                val instantSpeed = ((routeDistance - lastDistanceComputingInstantSpeed) / 1000f) / (deltaTime / 3600000f)
                routeTrackingState.setInstantSpeed(instantSpeed)
            }

            lastTimeComputingInstantSpeed = currentTime
            lastDistanceComputingInstantSpeed = routeDistance
        }
    }

    private suspend fun computeRouteDistance(locations: List<Location>): Double {
        val computeLengthLocations = mutableListOf<Location>()
        lastComputeLengthLocation ?. let {
            computeLengthLocations.add(it)
        }
        computeLengthLocations.addAll(locations)
        val locationUpdateDistance = SphericalUtil.computeLength(computeLengthLocations.map { it.toGmsLatLng() })
        lastComputeLengthLocation = locations.lastOrNull()
        val routeDistance = routeTrackingState.getRouteDistance() + locationUpdateDistance
        routeTrackingState.setRouteDistance(routeDistance)

        return routeDistance
    }

    private fun stopLocationUpdates() {
        locationUpdateJob?.cancel()
    }

    private fun createTrackingNotification() {
        val notificationIntent = RouteTrackingActivity.launchIntent(this)
        val pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT)

        val notification: Notification = NotificationCompat.Builder(this, NOTIF_CHANNEL_ID)
            .setContentTitle(getString(R.string.route_tracking_notification_title))
            .setContentText(getString(R.string.route_tracking_notification_content))
            .setSmallIcon(R.drawable.ic_run_circle)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        startForeground(NOTIF_ID_TRACKING, notification)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent == null) {
            mainScope.launch {
                // service restarted after process is killed
                if (routeTrackingState.isTrackingInProgress()) {
                    onActionResume(true)
                }
            }
        } else {
            when (intent.action) {
                ACTION_START -> onActionStart(intent)
                ACTION_STOP -> onActionStop()
                ACTION_RESUME -> onActionResume(false)
                ACTION_PAUSE -> onActionPause()
            }
        }

        return START_STICKY
    }

    private fun onActionStart(intent: Intent) {
        Timber.d("onActionStart")
        mainScope.launch {
            val startTime = System.currentTimeMillis()
            routeTrackingState.setTrackingInProgress(true)
            routeTrackingState.setTrackingStartTime(startTime)
            routeTrackingState.setLastResumeTime(startTime)
        }

        createTrackingNotification()
        requestLocationUpdates()
        createWakeLock()
    }

    private fun onActionPause() {
        locationUpdateJob?.cancel()
        saveTrackingDuration(System.currentTimeMillis())
    }

    private fun onActionResume(isServiceRestart: Boolean) {
        Timber.d("onActionResume isServiceRestart=$isServiceRestart")

        requestLocationUpdates()

        if (isServiceRestart) {
            ioScope.launch {
                saveTrackingDuration(routeTrackingLocationRepository.getLatestLocationTime())
            }
        } else {
            createTrackingNotification()
        }

        ioScope.launch {
            routeTrackingState.setLastResumeTime(System.currentTimeMillis())
        }
    }

    private fun onActionStop() {
        Timber.d("onActionStop")

        stopLocationUpdates()
        stopSelf()
//        saveTrackingDuration()
        mainScope.launch {
            routeTrackingState.clear()
            routeTrackingLocationRepository.clearRouteTrackingLocation()
        }
        releaseWakeLock()
    }

    private fun saveTrackingDuration(saveTime: Long) {
        ioScope.launch {
            val trackDuration = saveTime - routeTrackingState.getLastResumeTime()
            routeTrackingState.setTrackingDuration(routeTrackingState.getTrackingDuration() + trackDuration)
        }
    }

    private fun releaseWakeLock() {
        wakeLock?.let {
            if (it.isHeld)
                it.release()
            wakeLock = null
        }
    }

    private fun createWakeLock() {
        wakeLock = (getSystemService(Context.POWER_SERVICE) as PowerManager).run {
            newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "RidgeRunners::WakelockTag").apply {
                acquire()
            }
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(NOTIF_CHANNEL_ID, "Route Tracking", NotificationManager.IMPORTANCE_DEFAULT)
            channel.description = "While app is tracking your route"
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        const val NOTIF_ID_TRACKING = 101

        const val NOTIF_CHANNEL_ID = "NOTIF_CHANNEL_ID"

        const val LOCATION_UPDATE_INTERVAL = 3000L
        const val SMALLEST_DISPLACEMENT = 5f

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

        fun pauseIntent(context: Context) = Intent(context, RouteTrackingService::class.java).apply {
            action = ACTION_PAUSE
        }

        fun resumeIntent(context: Context) = Intent(context, RouteTrackingService::class.java).apply {
            action = ACTION_RESUME
        }

        fun createLocationTrackingRequest() = LocationRequest().apply {
            fastestInterval = LOCATION_UPDATE_INTERVAL
            interval = LOCATION_UPDATE_INTERVAL
            maxWaitTime = LOCATION_UPDATE_INTERVAL * 10
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            smallestDisplacement = SMALLEST_DISPLACEMENT
        }

        fun isTrackingServiceRunning(context: Context): Boolean {
            val activityMan = ContextCompat.getSystemService(context, ActivityManager::class.java)
            val infos = activityMan?.getRunningServices(Int.MAX_VALUE)
            return infos?.any { RouteTrackingService::class.java.name == it.service.className } == true
        }
    }
}