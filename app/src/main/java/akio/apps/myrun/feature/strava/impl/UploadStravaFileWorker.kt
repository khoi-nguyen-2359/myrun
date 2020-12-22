package akio.apps.myrun.feature.strava.impl

import akio.apps.myrun._di.androidInjector
import akio.apps.myrun.data.externalapp.StravaTokenStorage
import akio.apps.myrun.feature.strava.UploadActivityFilesToStravaUsecase
import android.content.Context
import androidx.work.*
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class UploadStravaFileWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    @Inject
    lateinit var uploadActivityFilesToStravaUsecase: UploadActivityFilesToStravaUsecase

    @Inject
    lateinit var stravaTokenStorage: StravaTokenStorage

    init {
        applicationContext.androidInjector.inject(this)
    }

    override suspend fun doWork(): Result {
        Timber.d("worker start")
        if (stravaTokenStorage.getToken() == null) {
            return Result.failure(workDataOf(
                OUTPUT_ERROR_MESSAGE to "Use has turned off strava sync"
            ))
        }

        val remaining = uploadActivityFilesToStravaUsecase.upload()
        if (remaining == 0) {
            WorkManager.getInstance(applicationContext).cancelWorkById(this.id)
        }

        return Result.success()
    }

    companion object {
        const val OUTPUT_ERROR_MESSAGE = "OUTPUT_ERROR_MESSAGE"

        const val UNIQUE_WORK_NAME = "UploadStravaFileWorker.WorkerName"

        fun enqueueDaily(context: Context) {
            enqueue(context, ExistingPeriodicWorkPolicy.KEEP)
        }

        fun enqueueForFinishedActivity(context: Context) {
            enqueue(context, ExistingPeriodicWorkPolicy.REPLACE)
        }

        private fun enqueue(context: Context, policy: ExistingPeriodicWorkPolicy) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.UNMETERED)
                .build()

            val workRequest = PeriodicWorkRequestBuilder<UploadStravaFileWorker>(1, TimeUnit.DAYS)
                .setConstraints(constraints)
                .build()
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                UNIQUE_WORK_NAME,
                policy,
                workRequest
            )
        }
    }
}