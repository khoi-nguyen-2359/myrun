package akio.apps.myrun.domain.routetracking

import akio.apps._base.error.UnauthorizedUserError
import akio.apps.myrun._base.utils.toGmsLatLng
import akio.apps.myrun.data.activity.ActivityRepository
import akio.apps.myrun.data.activity.model.ActivityDataModel
import akio.apps.myrun.data.activity.model.ActivityModel
import akio.apps.myrun.data.activity.model.ActivityType
import akio.apps.myrun.data.activity.model.CyclingActivityModel
import akio.apps.myrun.data.activity.model.RunningActivityModel
import akio.apps.myrun.data.authentication.UserAuthenticationState
import akio.apps.myrun.data.fitness.FitnessDataRepository
import akio.apps.myrun.data.fitness.SingleDataPoint
import akio.apps.myrun.data.location.LocationEntity
import akio.apps.myrun.data.routetracking.RouteTrackingLocationRepository
import akio.apps.myrun.data.routetracking.RouteTrackingState
import akio.apps.myrun.data.userprofile.UserProfileRepository
import android.graphics.Bitmap
import com.google.maps.android.PolyUtil
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class SaveRouteTrackingActivityUsecase @Inject constructor(
    private val routeTrackingState: RouteTrackingState,
    private val routeTrackingLocationRepository: RouteTrackingLocationRepository,
    private val activityRepository: ActivityRepository,
    private val userAuthenticationState: UserAuthenticationState,
    private val userProfileRepository: UserProfileRepository,
    private val fitnessDataRepository: FitnessDataRepository
) {

    suspend fun saveCurrentActivity(
        activityType: ActivityType,
        routeMapImage: Bitmap
    ): ActivityModel {
        val userAccountId = userAuthenticationState.getUserAccountId()
            ?: throw UnauthorizedUserError()

        val userProfile = userProfileRepository.getUserProfile(userAccountId)

        val endTime = System.currentTimeMillis()
        val startTime = routeTrackingState.getTrackingStartTime()
        val duration = routeTrackingState.getTrackingDuration()
        val distance = routeTrackingState.getRouteDistance()
        val trackedLocations = routeTrackingLocationRepository.getAllLocations()
        val encodedPolyline = PolyUtil.encode(trackedLocations.map { it.toGmsLatLng() })
        val activityData = ActivityDataModel(
            id = "",
            activityType,
            name = "",
            routeImage = "",
            startTime,
            endTime,
            duration,
            distance,
            encodedPolyline,
            athleteInfo = ActivityModel.AthleteInfo(
                userProfile.accountId,
                userProfile.name,
                userProfile.photo,
            )
        )

        val speedDataPoints =
            fitnessDataRepository.getSpeedDataPoints(startTime, endTime, FITNESS_DATA_INTERVAL)
        val stepCadenceDataPoints: List<SingleDataPoint<Int>>? =
            if (activityType == ActivityType.Running) {
                fitnessDataRepository.getSteppingCadenceDataPoints(
                    startTime,
                    endTime,
                    FITNESS_DATA_INTERVAL
                )
            } else {
                null
            }
        val locationDataPoints = trackedLocations.map {
            SingleDataPoint(
                it.time,
                LocationEntity(it.latitude, it.longitude, it.altitude)
            )
        }
        val savingActivity: ActivityModel = when (activityType) {
            ActivityType.Running -> {
                val pace = (duration / (1000 * 60)) / (distance / 1000)
                val cadence = stepCadenceDataPoints
                    ?.takeIf { it.isNotEmpty() }
                    ?.run { sumOf { it.value } / size }
                    ?: 0
                RunningActivityModel(activityData, pace, cadence)
            }

            ActivityType.Cycling -> {
                val speed = (distance / 1000) / (TimeUnit.MILLISECONDS.toHours(duration))
                CyclingActivityModel(activityData, speed)
            }

            else -> throw UnsupportedOperationException(
                "Saving unknown activity type $activityType"
            )
        }

        val activityId = activityRepository.saveActivity(
            savingActivity,
            routeMapImage,
            speedDataPoints,
            stepCadenceDataPoints,
            locationDataPoints
        )

        val activityDataWithId = activityData.copy(id = activityId)
        return when (savingActivity) {
            is RunningActivityModel -> savingActivity.copy(activityData = activityDataWithId)
            is CyclingActivityModel -> savingActivity.copy(activityData = activityDataWithId)
            else -> throw UnsupportedOperationException("Unknown activity type")
        }
    }

    companion object {
        const val FITNESS_DATA_INTERVAL = 3000L
    }
}
