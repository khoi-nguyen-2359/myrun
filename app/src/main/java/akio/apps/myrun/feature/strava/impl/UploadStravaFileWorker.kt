package akio.apps.myrun.feature.strava.impl

import akio.apps.myrun.data.authentication.UserAuthenticationState
import akio.apps.myrun.data.externalapp.ExternalAppProvidersRepository
import akio.apps.myrun.domain.strava.UploadActivityFilesToStravaUsecase
import akio.apps.myrun.feature.strava._di.DaggerStravaFeatureComponent
import android.app.Application
import android.content.Context
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.workDataOf
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
    lateinit var externalAppProvidersRepository: ExternalAppProvidersRepository

    @Inject
    lateinit var userAuthenticationState: UserAuthenticationState

    init {
        DaggerStravaFeatureComponent.factory().create(appContext as Application).inject(this)
    }

    override suspend fun doWork(): Result {
        Timber.d("worker start")
        val userAccountId = userAuthenticationState.getUserAccountId()
        if (userAccountId == null ||
            externalAppProvidersRepository.getStravaProviderToken(userAccountId) == null
        ) {
            return Result.failure(
                workDataOf(
                    OUTPUT_ERROR_MESSAGE to "Use has turned off strava sync"
                )
            )
        }

        val remaining = uploadActivityFilesToStravaUsecase.upload()
        if (remaining == 0) {
            WorkManager.getInstance(applicationContext)
                .cancelWorkById(this.id)
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
            WorkManager.getInstance(context)
                .enqueueUniquePeriodicWork(
                    UNIQUE_WORK_NAME,
                    policy,
                    workRequest
                )
        }
    }
}
