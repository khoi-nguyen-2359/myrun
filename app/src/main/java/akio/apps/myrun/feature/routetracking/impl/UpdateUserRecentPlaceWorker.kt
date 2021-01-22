package akio.apps.myrun.feature.routetracking.impl

import akio.apps.myrun._di.androidInjector
import akio.apps.myrun.feature.routetracking.UpdateUserRecentPlaceUsecase
import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import javax.inject.Inject

class UpdateUserRecentPlaceWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    @Inject
    lateinit var updateUserRecentPlaceUsecase: UpdateUserRecentPlaceUsecase

    init {
        applicationContext.androidInjector.inject(this)
    }

    override suspend fun doWork(): Result {
        val lat = inputData.getDouble(INPUT_START_LOCATION_LAT, 0.0)
        val lng = inputData.getDouble(INPUT_START_LOCATION_LNG, 0.0)
        return try {
            updateUserRecentPlaceUsecase.updateUserRecentPlace(lat, lng)
            Result.success()
        } catch (throwable: Throwable) {
            Result.failure()
        }
    }

    companion object {
        const val INPUT_START_LOCATION_LAT = "INPUT_START_LOCATION_LAT"
        const val INPUT_START_LOCATION_LNG = "INPUT_START_LOCATION_LNG"
    }
}
