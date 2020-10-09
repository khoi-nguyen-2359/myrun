package akio.apps.myrun.feature.routetracking.usecase

import akio.apps._base.error.UnauthorizedUserError
import akio.apps.myrun.data.activity.*
import akio.apps.myrun.data.authentication.UserAuthenticationState
import akio.apps.myrun.data.place.PlaceEntity
import akio.apps.myrun.data.routetracking.RouteTrackingLocationRepository
import akio.apps.myrun.data.routetracking.RouteTrackingState
import akio.apps.myrun.feature._base.utils.toGmsLatLng
import akio.apps.myrun.feature.routetracking.SaveRouteTrackingActivityUsecase
import android.graphics.Bitmap
import com.google.maps.android.PolyUtil
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class SaveRouteTrackingActivityUsecaseImpl @Inject constructor(
    private val routeTrackingState: RouteTrackingState,
    private val routeTrackingLocationRepository: RouteTrackingLocationRepository,
    private val activityRepository: ActivityRepository,
    private val userAuthenticationState: UserAuthenticationState
) : SaveRouteTrackingActivityUsecase {

    override suspend fun saveCurrentActivity(activityType: ActivityType, routeMapImage: Bitmap) {
        val userAccount = userAuthenticationState.getUserAccount()
            ?: throw UnauthorizedUserError()

        val endTime = System.currentTimeMillis()
        val startTime = routeTrackingState.getTrackingStartTime()
        val duration = routeTrackingState.getTrackingDuration()
        val distance = routeTrackingState.getRouteDistance()
        val encodedPolyline = PolyUtil.encode(routeTrackingLocationRepository.getAllLocations().map { it.toGmsLatLng() })
        val activityData: ActivityEntity = ActivityDataEntity("", userAccount.uid, activityType, "", "", startTime, endTime, duration, distance, encodedPolyline)

        val savingActivity = when (activityType) {
            ActivityType.Running -> {
                val pace = (duration / (1000 * 60)) / (distance / 1000)
                RunningActivityEntity(activityData, pace)
            }

            ActivityType.Cycling -> {
                val speed = (distance / 1000) / (TimeUnit.MILLISECONDS.toHours(duration))
                CyclingActivityEntity(activityData, speed)
            }

            else -> return  // stop saving for unknown type
        }

        activityRepository.saveActivity(savingActivity, routeMapImage)
    }
}