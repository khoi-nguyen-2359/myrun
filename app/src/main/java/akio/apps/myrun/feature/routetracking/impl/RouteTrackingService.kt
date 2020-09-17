package akio.apps.myrun.feature.routetracking.impl

import akio.apps.myrun.R
import akio.apps.myrun.data.location.LocationDataSource
import akio.apps.myrun.data.location.dto.LocationRequestEntity
import akio.apps.myrun.data.routetracking.RouteTrackingLocationRepository
import akio.apps.myrun.data.routetracking.RouteTrackingState
import android.annotation.SuppressLint
import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationRequest
import com.google.firebase.crashlytics.FirebaseCrashlytics
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
            locationUpdateFlow.collect { locations ->
                Timber.d("collect locations: ${locations.size}")
                routeTrackingLocationRepository.insert(locations)
            }
        }
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
                if (routeTrackingState.isRouteTrackingInProgress()) {
                    onActionResume(false)
                }
            }
        } else {
            when (intent.action) {
                ACTION_START -> onActionStart(intent)
                ACTION_STOP -> onActionStop()
                ACTION_RESUME -> onActionResume(true)
                ACTION_PAUSE -> onActionPause()
            }
        }

        return START_STICKY
    }

    private fun onActionPause() {
        locationUpdateJob?.cancel()
    }

    private fun onActionStop() {
        Timber.d("onActionStop")

        stopLocationUpdates()
        stopSelf()
        mainScope.launch {
            routeTrackingState.setRouteTrackingInProgress(false)
        }
        releaseWakeLock()
    }

    private fun releaseWakeLock() {
        wakeLock?.let {
            if (it.isHeld)
                it.release()
            wakeLock = null
        }
    }

    private fun onActionResume(manualResume: Boolean) {
        Timber.d("onActionResume")

        requestLocationUpdates()

        if (manualResume) {
            createTrackingNotification()
        }
    }

    private fun onActionStart(intent: Intent) {
        Timber.d("onActionStart")
        mainScope.launch {
            routeTrackingState.setRouteTrackingInProgress(true)
            routeTrackingLocationRepository.clearRouteTrackingLocation()
        }

        createTrackingNotification()
        requestLocationUpdates()
        createWakeLock()
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

        const val LOCATION_UPDATE_INTERVAL = 5L

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
            fastestInterval = LOCATION_UPDATE_INTERVAL * 200
            interval = LOCATION_UPDATE_INTERVAL * 500
            maxWaitTime = LOCATION_UPDATE_INTERVAL * 1000
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            smallestDisplacement = 5f
        }

        fun isTrackingServiceRunning(context: Context): Boolean {
            val activityMan = ContextCompat.getSystemService(context, ActivityManager::class.java)
            val infos = activityMan?.getRunningServices(Int.MAX_VALUE)
            return infos?.any { RouteTrackingService::class.java.name == it.service.className } == true
        }
    }
}