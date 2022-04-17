package akio.apps.myrun.feature.activitydetail

import akio.apps.myrun.domain.activity.ExportTempTcxFileUsecase
import akio.apps.myrun.feature.activity.R
import akio.apps.myrun.feature.activitydetail.di.DaggerActivityExportFeatureComponent
import akio.apps.myrun.feature.core.AppNotificationChannel
import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.os.Parcelable
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.FileProvider
import java.io.File
import java.text.SimpleDateFormat
import java.util.concurrent.ConcurrentLinkedQueue
import javax.inject.Inject
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import timber.log.Timber

class ActivityExportService : Service() {

    @Inject
    lateinit var exportTempTcxFileUsecase: ExportTempTcxFileUsecase

    private val activityInfoQueue = ConcurrentLinkedQueue<ActivityInfo>()
    private var processingJob: Job? = null

    private val exceptionHandler = CoroutineExceptionHandler { _, exception ->
        Timber.e(exception)
    }

    private val mainScope = CoroutineScope(SupervisorJob() + Dispatchers.Main + exceptionHandler)

    private val activityStartTimeFormatter = SimpleDateFormat("MMM dd, yyyy")

    override fun onCreate() {
        super.onCreate()
        DaggerActivityExportFeatureComponent.factory().create(application).inject(this)
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
        NotificationManagerCompat.from(this).cancel(activityInfo.notificationId)
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
        val notificationContentText = if (processingCount > 0) {
            getString(
                R.string.activity_export_progress_notification_remaining,
                processingCount
            )
        } else {
            getString(R.string.activity_export_progress_notification_finishing)
        }
        val notificationBuilder = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            @Suppress("DEPRECATION")
            Notification.Builder(this)
        } else {
            Notification.Builder(this, AppNotificationChannel.General.id)
        }
            .setSmallIcon(R.drawable.ic_run_circle)
            .setContentTitle(getString(R.string.activity_export_progress_notification_title))
            .setContentText(notificationContentText)
            .setProgress(/*max = */0, /*progress = */0, /*indeterminate = */true)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            notificationBuilder.setForegroundServiceBehavior(
                Notification.FOREGROUND_SERVICE_IMMEDIATE
            )
        }
        startForeground(NOTIFICATION_ID_PROGRESS, notificationBuilder.build())
    }

    private suspend fun exportActivityList() = withContext(Dispatchers.IO) {
        while (true) {
            val activityInfo = activityInfoQueue.peek()
                ?: break
            val exportedFile = exportTempTcxFileUsecase(activityInfo.id)
            // reduce the queue at this place for correct counter on the progress notification
            // message
            activityInfoQueue.poll()
            updateProgressNotification()
            if (exportedFile != null) {
                notifyExportSuccess(activityInfo, exportedFile)
            } else {
                notifyExportError(activityInfo)
            }
        }
        stopSelf()
    }

    private fun notifyExportError(activityInfo: ActivityInfo) {
        val activityDescription =
            createActivityDescription(activityInfo)
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
        val notification = NotificationCompat.Builder(this, AppNotificationChannel.General.id)
            .setSmallIcon(R.drawable.ic_run_circle)
            .setContentTitle(getString(R.string.activity_export_error_notification_title))
            .setContentText(activityDescription)
            .addAction(action)
            .build()
        NotificationManagerCompat.from(this)
            .notify(activityInfo.notificationId, notification)
    }

    private fun notifyExportSuccess(activityInfo: ActivityInfo, exportedFile: File) {
        val activityDescription =
            createActivityDescription(activityInfo)
        val shareFileContentUri = FileProvider.getUriForFile(
            this,
            this.packageName,
            exportedFile
        )
        val pendingIntentFlag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_ONE_SHOT
        }
        val sendIntent = Intent(Intent.ACTION_SEND)
        sendIntent.putExtra(Intent.EXTRA_STREAM, shareFileContentUri)
        sendIntent.type = "*/*"
        sendIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        val chooserIntent = Intent.createChooser(
            sendIntent,
            getString(R.string.activity_export_send_tracklog_title, activityDescription)
        )
        chooserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            chooserIntent,
            pendingIntentFlag
        )
        val notification = NotificationCompat.Builder(this, AppNotificationChannel.General.id)
            .setSmallIcon(R.drawable.ic_run_circle)
            .setContentTitle(activityDescription)
            .setContentText(getString(R.string.activity_export_success_notification_title))
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()
        NotificationManagerCompat.from(this)
            .notify(activityInfo.notificationId, notification)
    }

    private fun createActivityDescription(activityInfo: ActivityInfo): String {
        val activityFormattedStartTime =
            activityStartTimeFormatter.format(activityInfo.startTime)
        return "${activityInfo.name} on $activityFormattedStartTime"
    }

    /**
     * Information to process an activity, display progress/result of exporting on notifications.
     */
    @Parcelize
    data class ActivityInfo(
        val id: String,
        val name: String,
        val startTime: Long,
    ) : Parcelable {

        /**
         * Generates notification id from this activity's start time.
         */
        @IgnoredOnParcel
        val notificationId: Int = startTime.toInt()
    }

    companion object {
        private val NOTIFICATION_ID_PROGRESS =
            AppNotificationChannel.General.nextNotificationStaticId()

        private const val EXTRA_ACTIVITY_INFO = "EXTRA_ACTIVITY_INFO"

        private const val ACTION_ADD_ACTIVITY =
            "akio.apps.myrun.feature.activityexport.ExportActivityService.ACTION_ADD_ACTIVITY_ID"

        private const val REQUEST_CODE_ADD_ACTIVITY = 1

        fun createAddActivityIntent(context: Context, activityInfo: ActivityInfo): Intent =
            Intent(context, ActivityExportService::class.java)
                .setAction(ACTION_ADD_ACTIVITY)
                .putExtra(EXTRA_ACTIVITY_INFO, activityInfo)
    }
}
