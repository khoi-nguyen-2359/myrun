package akio.apps.myrun.feature.routetracking.usecase

import akio.apps.myrun.data.routetracking.RouteTrackingLocationRepository
import akio.apps.myrun.data.routetracking.RouteTrackingState
import akio.apps.myrun.data.activity.ActivityRepository
import akio.apps.myrun.data.activity.ActivityType
import akio.apps.myrun.data.activity.RunningActivityEntity
import akio.apps.myrun.data.activity.ActivityEntity
import akio.apps.myrun.data.activity.ActivityDataEntity
import akio.apps.myrun.feature._base.utils.toGmsLatLng
import akio.apps.myrun.feature.routetracking.SaveRouteTrackingActivityUsecase
import android.graphics.Bitmap
import com.google.maps.android.PolyUtil
import javax.inject.Inject

class SaveRouteTrackingActivityUsecaseImpl @Inject constructor(
    private val routeTrackingState: RouteTrackingState,
    private val routeTrackingLocationRepository: RouteTrackingLocationRepository,
    private val activityRepository: ActivityRepository
) : SaveRouteTrackingActivityUsecase {

    override suspend fun saveCurrentActivity(activityType: ActivityType, routeMapImage: Bitmap) {
        val endTime = System.currentTimeMillis()
        val startTime = routeTrackingState.getTrackingStartTime()
        val duration = routeTrackingState.getTrackingDuration()
        val activityData: ActivityEntity = ActivityDataEntity("", activityType, startTime, endTime, duration)

        val savingActivity = when (activityType) {
            ActivityType.Running -> {
                val distance = routeTrackingState.getRouteDistance()
                val averagePace = (duration / (1000 * 60)) / (distance / 1000)
                val encodedLocations = PolyUtil.encode(routeTrackingLocationRepository.getAllLocations().map { it.toGmsLatLng() })

                RunningActivityEntity(
                    activityData = activityData,
                    averagePace = averagePace,
                    distance = distance,
                    encodedPolyline = encodedLocations,
                    routePhoto = "",
                )
            }

            else -> return  // stop saving for unknown type
        }

        activityRepository.saveActivity(savingActivity, routeMapImage)
    }
}