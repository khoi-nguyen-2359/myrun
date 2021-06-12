package akio.apps.myrun.feature.routetracking.impl

import akio.apps.myrun.R
import akio.apps.myrun.data.activity.ActivityRepository
import akio.apps.myrun.data.activitysharing.ActivityLocalStorage
import akio.apps.myrun.data.fitness.SingleDataPoint
import akio.apps.myrun.data.location.LocationEntity
import akio.apps.myrun.feature.routetracking._di.DaggerRouteTrackingFeatureComponent
import akio.apps.myrun.feature.routetracking.impl.RouteTrackingService.Companion.NOTIF_CHANNEL_ID
import android.app.Notification
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.Date
import javax.inject.Inject
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class ActivityUploadService : Service() {

    @Inject
    lateinit var activityRepository: ActivityRepository

    @Inject
    lateinit var activityLocalStorage: ActivityLocalStorage

    private val activityStartTimeFormatter = SimpleDateFormat("MMM dd, yyyy")

    private val exceptionHandler = CoroutineExceptionHandler { _, exception ->
        Timber.e(exception)
    }

    private val mainScope = CoroutineScope(SupervisorJob() + Dispatchers.Main + exceptionHandler)

    override fun onCreate() {
        DaggerRouteTrackingFeatureComponent.factory().create(application).inject(this)
        super.onCreate()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        uploadActivity()
        return START_STICKY
    }

    private fun uploadActivity() = mainScope.launch {
        activityLocalStorage.getStoredActivityIds().forEach { activityId ->
            val storageOutput = activityLocalStorage.loadActivityData(activityId)
            val activity = storageOutput.activityModel
            notifyForegroundNotification(activity.name, activity.startTime)
            activityRepository.saveActivity(
                activity,
                storageOutput.routeBitmapFile,
                speedDataPoints = emptyList(),
                stepCadenceDataPoints = emptyList(),
                storageOutput.locationDataPoints.map {
                    SingleDataPoint(
                        it.time,
                        LocationEntity(it.latitude, it.longitude, it.altitude)
                    )
                }
            )
            activityLocalStorage.deleteActivityData(activityId)
        }
        stopSelf()
    }

    private fun notifyForegroundNotification(activityName: String, activityStartTime: Long) {
        val notificationContent =
            "$activityName on ${activityStartTimeFormatter.format(Date(activityStartTime))}"
        val notificationBuilder: Notification.Builder =
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
                Notification.Builder(this)
            } else {
                Notification.Builder(
                    this,
                    NOTIF_CHANNEL_ID
                )
            }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            notificationBuilder.setForegroundServiceBehavior(
                Notification.FOREGROUND_SERVICE_IMMEDIATE
            )
        }
        val notification: Notification = notificationBuilder
            .setContentTitle(getString(R.string.activity_upload_notification_title))
            .setProgress(0, 0, true)
            .setContentText(notificationContent)
            .setSmallIcon(R.drawable.ic_run_circle)
            .setOnlyAlertOnce(true)
            .build()

        startForeground(NOTIFICATION_ID, notification)
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        private const val NOTIFICATION_ID = 201

        fun createStartIntent(context: Context): Intent =
            Intent(context, ActivityUploadService::class.java)
    }

}
