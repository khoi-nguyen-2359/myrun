package akio.apps.myrun.feature.routetracking.impl

import akio.apps.myrun.data.location.LocationDataSource
import akio.apps.myrun.domain.recentplace.UpdateUserRecentPlaceUsecase
import akio.apps.myrun.feature.routetracking._di.DaggerRouteTrackingFeatureComponent
import akio.apps.myrun.feature.strava.impl.UploadStravaFileWorker
import android.app.Application
import android.content.Context
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class UpdateUserRecentPlaceWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    @Inject
    lateinit var updateUserRecentPlaceUsecase: UpdateUserRecentPlaceUsecase

    @Inject
    lateinit var locationDataSource: LocationDataSource

    init {
        DaggerRouteTrackingFeatureComponent.factory()
            .create(appContext.applicationContext as Application)
            .inject(this)
    }

    override suspend fun doWork(): Result {
        val lastLocation = locationDataSource.getLastLocation()
            ?: return Result.retry()

        val lat = lastLocation.latitude
        val lng = lastLocation.longitude
        return try {
            updateUserRecentPlaceUsecase(lat, lng)
            Result.success()
        } catch (throwable: Throwable) {
            Result.retry()
        }
    }

    companion object {
        private const val UNIQUE_WORK_NAME = "routetracking.UpdateUserRecentPlaceWorker"

        fun enqueueDaily(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.UNMETERED)
                .build()

            val workRequest = PeriodicWorkRequestBuilder<UploadStravaFileWorker>(
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
    }
}
