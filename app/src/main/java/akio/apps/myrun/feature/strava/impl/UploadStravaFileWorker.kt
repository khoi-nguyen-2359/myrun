package akio.apps.myrun.feature.strava.impl

import akio.apps.myrun.domain.strava.UploadActivityFilesToStravaUsecase
import akio.apps.myrun.feature.strava._di.DaggerStravaFeatureComponent
import android.content.Context
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import javax.inject.Inject
import timber.log.Timber

class UploadStravaFileWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    @Inject
    lateinit var uploadActivityFilesToStravaUsecase: UploadActivityFilesToStravaUsecase

    init {
        DaggerStravaFeatureComponent.factory().create().inject(this)
    }

    override suspend fun doWork(): Result {
        Timber.d("worker start")
        val isComplete = uploadActivityFilesToStravaUsecase.uploadAll()
        return if (isComplete) {
            Result.success()
        } else {
            Result.retry()
        }
    }

    companion object {
        const val UNIQUE_WORK_NAME = "UploadStravaFileWorker.WorkerName"

        fun enqueue(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.UNMETERED)
                .setRequiresBatteryNotLow(true)
                .build()

            val workRequest = OneTimeWorkRequestBuilder<UploadStravaFileWorker>()
                .setConstraints(constraints)
                .build()
            WorkManager.getInstance(context)
                .enqueueUniqueWork(UNIQUE_WORK_NAME, ExistingWorkPolicy.KEEP, workRequest)
        }

        fun clear(context: Context) =
            WorkManager.getInstance(context).cancelUniqueWork(UNIQUE_WORK_NAME)
    }
}
