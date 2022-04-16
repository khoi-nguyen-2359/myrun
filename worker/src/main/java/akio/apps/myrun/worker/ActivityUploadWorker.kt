package akio.apps.myrun.worker

import akio.apps.myrun.data.common.Resource
import akio.apps.myrun.data.common.di.NamedIoDispatcher
import akio.apps.myrun.domain.activity.UploadActivitiesUsecase
import akio.apps.myrun.feature.base.AppNotificationChannel
import android.app.Application
import android.app.Notification
import android.content.Context
import android.os.Build
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.ForegroundInfo
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import java.text.SimpleDateFormat
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onEach

class ActivityUploadWorker(appContext: Context, params: WorkerParameters) :
    CoroutineWorker(appContext, params) {

    @Inject
    lateinit var uploadActivitiesUsecase: UploadActivitiesUsecase

    @Inject
    @NamedIoDispatcher
    lateinit var ioDispatcher: CoroutineDispatcher

    private val activityStartTimeFormatter = SimpleDateFormat("MMM dd, yyyy")

    init {
        DaggerWorkerFeatureComponent.factory().create(appContext as Application).inject(this)
    }

    override suspend fun doWork(): Result {
        val isCompleted = uploadActivitiesUsecase.uploadAll()
            .flowOn(ioDispatcher)
            .onEach { resource ->
                if (resource is Resource.Success) {
                    setForegroundAsync(
                        createForegroundInfo(resource.data.name, resource.data.startTime)
                    )
                }
            }
            .firstOrNull { it is Resource.Error } == null
        return if (isCompleted) {
            Result.success()
        } else {
            Result.retry()
        }
    }

    private fun createForegroundInfo(
        activityName: String,
        activityStartTime: Long,
    ): ForegroundInfo {
        val notificationContent =
            "$activityName on ${activityStartTimeFormatter.format(activityStartTime)}"
        val notificationBuilder: Notification.Builder =
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
                @Suppress("DEPRECATION")
                Notification.Builder(applicationContext)
            } else {
                Notification.Builder(applicationContext, AppNotificationChannel.General.id)
            }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            notificationBuilder.setForegroundServiceBehavior(
                Notification.FOREGROUND_SERVICE_IMMEDIATE
            )
        }
        val contentTitle = applicationContext.getString(R.string.activity_upload_notification_title)
        val notification: Notification = notificationBuilder
            .setContentTitle(contentTitle)
            .setProgress(0, 0, true)
            .setContentText(notificationContent)
            .setSmallIcon(R.drawable.ic_run_circle)
            .setOnlyAlertOnce(true)
            .build()

        return ForegroundInfo(NOTIFICATION_ID, notification)
    }

    companion object {
        private val NOTIFICATION_ID = AppNotificationChannel.General.nextNotificationStaticId()
        private const val UNIQUE_WORK_NAME = "ActivityUploadWorker"

        fun enqueue(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val workRequest = OneTimeWorkRequestBuilder<ActivityUploadWorker>()
                .setBackoffCriteria(BackoffPolicy.LINEAR, 1 /* backoffDelay */, TimeUnit.MINUTES)
                .setConstraints(constraints)
                .build()
            WorkManager.getInstance(context)
                .enqueueUniqueWork(
                    UNIQUE_WORK_NAME,
                    ExistingWorkPolicy.APPEND_OR_REPLACE,
                    workRequest
                )
        }

        fun clear(context: Context) =
            WorkManager.getInstance(context).cancelUniqueWork(UNIQUE_WORK_NAME)
    }
}
