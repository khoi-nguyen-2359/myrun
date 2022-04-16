package akio.apps.myrun.worker

import akio.apps.myrun.data.common.di.NamedIoDispatcher
import akio.apps.myrun.domain.strava.UploadActivityFilesToStravaUsecase
import android.app.Application
import android.content.Context
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import timber.log.Timber

class UploadStravaFileWorker(
    appContext: Context,
    params: WorkerParameters,
) : CoroutineWorker(appContext, params) {

    @Inject
    lateinit var uploadActivityFilesToStravaUsecase: UploadActivityFilesToStravaUsecase

    @Inject
    @NamedIoDispatcher
    lateinit var ioDispatcher: CoroutineDispatcher

    init {
        DaggerWorkerFeatureComponent.factory().create(appContext as Application).inject(this)
    }

    override suspend fun doWork(): Result {
        Timber.d("worker start")
        val isComplete = withContext(ioDispatcher) {
            uploadActivityFilesToStravaUsecase.uploadAll()
        }
        return if (isComplete) {
            Result.success()
        } else {
            Result.retry()
        }
    }

    companion object {
        private const val UNIQUE_WORK_NAME = "UploadStravaFileWorker.WorkerName"

        fun enqueue(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.UNMETERED)
                .setRequiresBatteryNotLow(true)
                .build()

            val workRequest = OneTimeWorkRequestBuilder<UploadStravaFileWorker>()
                .setConstraints(constraints)
                .setBackoffCriteria(BackoffPolicy.LINEAR, 1 /* backoffDelay */, TimeUnit.HOURS)
                .build()
            WorkManager.getInstance(context)
                .enqueueUniqueWork(UNIQUE_WORK_NAME, ExistingWorkPolicy.KEEP, workRequest)
        }

        fun clear(context: Context) =
            WorkManager.getInstance(context).cancelUniqueWork(UNIQUE_WORK_NAME)
    }
}
