package akio.apps.myrun.feature.activityexport

import akio.apps.myrun.R
import akio.apps.myrun.data.activityexport.model.TrackingRecord
import akio.apps.myrun.domain.activityexport.ExportActivityToTempTcxFileUsecase
import akio.apps.myrun.feature.strava._di.DaggerStravaFeatureComponent
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.os.Parcelable
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.Date
import java.util.concurrent.ConcurrentLinkedQueue
import javax.inject.Inject
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.parcelize.Parcelize

class ActivityExportService : Service() {

    @Inject
    lateinit var exportActivityToTempTcxFileUsecase: ExportActivityToTempTcxFileUsecase

    private val activityInfoQueue = ConcurrentLinkedQueue<ActivityInfo>()
    private var processingJob: Job? = null

    private val exceptionHandler = CoroutineExceptionHandler { _, exception ->
        Timber.e(exception)
    }

    private val mainScope = CoroutineScope(SupervisorJob() + Dispatchers.Main + exceptionHandler)

    private val activityStartTimeFormatter = SimpleDateFormat("MMM dd, yyyy")

    override fun onCreate() {
        super.onCreate()
        DaggerStravaFeatureComponent.factory().create(this.application).inject(this)
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_ADD_ACTIVITY) {
            val activityInfo = intent.getParcelableExtra<ActivityInfo>(EXTRA_ACTIVITY_INFO)
            if (activityInfo != null) {
                onAddActivity(activityInfo)
            }
        }
        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun onAddActivity(activityInfo: ActivityInfo) {
        // this maybe a retrying so cancel the previous error notification
        NotificationManagerCompat.from(this).cancel(activityInfo.startTime.toInt())
        activityInfoQueue.add(activityInfo)
        updateProgressNotification()
        if (processingJob?.isActive != true) {
            processingJob = mainScope.launch {
                exportActivityList()
            }
        }
    }

    private fun updateProgressNotification() {
        val processingCount = activityInfoQueue.size
        if (processingCount == 0) {
            NotificationManagerCompat.from(this).cancel(NOTIFICATION_ID_PROGRESS)
        } else {
            val notificationContentText = getString(
                R.string.activity_export_progress_notification_remaining,
                processingCount
            )
            val notification = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_run_circle)
                .setContentTitle(getString(R.string.activity_export_progress_notification_title))
                .setContentText(notificationContentText)
                .setProgress(/*max = */0, /*progress = */0, /*indeterminate = */true)
                .build()
            startForeground(NOTIFICATION_ID_PROGRESS, notification)
        }
    }

    private suspend fun exportActivityList() = withContext(Dispatchers.IO) {
        while (true) {
            val activityInfo = activityInfoQueue.peek() ?: break
            val trackingRecord = exportActivityToTempTcxFileUsecase(activityInfo.id)
            activityInfoQueue.poll()    // reduce the queue at this place for correct counter on the progress notification message
            updateProgressNotification()
            if (trackingRecord != null) {
                notifyExportSuccess(trackingRecord)
            } else {
                notifyExportError(activityInfo)
            }
        }
        stopSelf()
    }

    private fun notifyExportError(activityInfo: ActivityInfo) {
        val activityFormattedStartTime =
            activityStartTimeFormatter.format(Date(activityInfo.startTime))
        val activityDescription = "${activityInfo.name} on $activityFormattedStartTime"
        val intent = createAddActivityIntent(this, activityInfo)
        val pendingIntentFlag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_ONE_SHOT
        }
        val pendingIntent = PendingIntent.getService(
            this,
            REQUEST_CODE_ADD_ACTIVITY,
            intent,
            pendingIntentFlag
        )
        val action = NotificationCompat.Action(
            /*icon = */ null,
            /*title = */ getString(R.string.action_retry),
            /*intent = */ pendingIntent
        )
        val notification = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_run_circle)
            .setContentTitle(getString(R.string.activity_export_error_notification_title))
            .setContentText(activityDescription)
            .addAction(action)
            .build()
        NotificationManagerCompat.from(this)
            .notify(activityInfo.startTime.toInt(), notification)
    }

    private fun notifyExportSuccess(trackingRecord: TrackingRecord) {
        val activityFormattedStartTime =
            activityStartTimeFormatter.format(Date(trackingRecord.activityStartTime))
        val activityDescription = "${trackingRecord.activityName} on $activityFormattedStartTime"
        val notification = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_run_circle)
            .setContentTitle(getString(R.string.activity_export_success_notification_title))
            .setContentText(activityDescription)
            .build()
        NotificationManagerCompat.from(this)
            .notify(trackingRecord.activityStartTime.toInt(), notification)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                "Export user activity",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            channel.description = "User activity exporting notifications."
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    /**
     * Information to process an activity, display progress/result of exporting on notifications.
     */
    @Parcelize
    data class ActivityInfo(
        val id: String,
        val name: String,
        val startTime: Long
    ) : Parcelable

    companion object {
        private const val NOTIFICATION_CHANNEL_ID =
            "akio.apps.myrun.feature.activityexport.ExportActivityService.NOTIFICATION_CHANNEL_ID"
        private const val EXTRA_ACTIVITY_INFO = "EXTRA_ACTIVITY_INFO"
        private const val ACTION_ADD_ACTIVITY =
            "akio.apps.myrun.feature.activityexport.ExportActivityService.ACTION_ADD_ACTIVITY_ID"

        private const val REQUEST_CODE_ADD_ACTIVITY = 1
        private const val NOTIFICATION_ID_PROGRESS = 201

        fun createAddActivityIntent(context: Context, activityInfo: ActivityInfo): Intent =
            Intent(context, ActivityExportService::class.java)
                .setAction(ACTION_ADD_ACTIVITY)
                .putExtra(EXTRA_ACTIVITY_INFO, activityInfo)
    }
}
