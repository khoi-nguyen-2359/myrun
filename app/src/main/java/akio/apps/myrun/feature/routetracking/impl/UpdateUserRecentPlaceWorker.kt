package akio.apps.myrun.feature.routetracking.impl

import akio.apps.myrun.data.location.api.LocationDataSource
import akio.apps.myrun.domain.recentplace.UpdateUserRecentPlaceUsecase
import akio.apps.myrun.feature.routetracking._di.DaggerRouteTrackingFeatureComponent
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
import okio.IOException
import timber.log.Timber

class UpdateUserRecentPlaceWorker(
    appContext: Context,
    params: WorkerParameters,
) : CoroutineWorker(appContext, params) {

    @Inject
    lateinit var updateUserRecentPlaceUsecase: UpdateUserRecentPlaceUsecase

    @Inject
    lateinit var locationDataSource: LocationDataSource

    init {
        DaggerRouteTrackingFeatureComponent.factory().create().inject(this)
    }

    override suspend fun doWork(): Result {
        val lastLocation = locationDataSource.getLastLocation()
            ?: return Result.retry()

        val lat = lastLocation.latitude
        val lng = lastLocation.longitude
        return try {
            updateUserRecentPlaceUsecase(lat, lng)
            Result.success()
        } catch (throwable: IOException) {
            Timber.e(throwable)
            Result.retry()
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
