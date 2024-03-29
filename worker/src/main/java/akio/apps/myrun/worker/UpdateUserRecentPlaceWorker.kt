package akio.apps.myrun.worker

import akio.apps.myrun.domain.user.UpdateUserRecentPlaceUsecase
import android.app.Application
import android.content.Context
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class UpdateUserRecentPlaceWorker(
    appContext: Context,
    params: WorkerParameters,
) : CoroutineWorker(appContext, params) {

    @Inject
    lateinit var updateUserRecentPlaceUsecase: UpdateUserRecentPlaceUsecase

    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO

    init {
        DaggerWorkerFeatureComponent.factory().create(appContext as Application).inject(this)
    }

    override suspend fun doWork(): Result = withContext(ioDispatcher) {
        when (updateUserRecentPlaceUsecase.updateUserRecentPlace()) {
            UpdateUserRecentPlaceUsecase.Result.InvalidUser -> Result.failure()
            UpdateUserRecentPlaceUsecase.Result.LocationUnavailable,
            UpdateUserRecentPlaceUsecase.Result.IOFailure,
            -> Result.retry()
            UpdateUserRecentPlaceUsecase.Result.Success -> Result.success()
        }
    }

    companion object {
        private const val UNIQUE_WORK_NAME = "routetracking.UpdateUserRecentPlaceWorker"

        fun enqueueDaily(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.UNMETERED)
                .build()

            val workRequest = PeriodicWorkRequestBuilder<UpdateUserRecentPlaceWorker>(
                repeatInterval = 1,
                TimeUnit.DAYS
            )
                .setConstraints(constraints)
                .build()
            WorkManager.getInstance(context)
                .enqueueUniquePeriodicWork(
                    UNIQUE_WORK_NAME,
                    ExistingPeriodicWorkPolicy.KEEP,
                    workRequest
                )
        }

        fun enqueueImmediately(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiresBatteryNotLow(true)
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val workRequest = OneTimeWorkRequestBuilder<UpdateUserRecentPlaceWorker>()
                .setConstraints(constraints)
                .build()
            WorkManager.getInstance(context)
                .enqueueUniqueWork(
                    UNIQUE_WORK_NAME,
                    ExistingWorkPolicy.REPLACE,
                    workRequest
                )
        }
    }
}
