package akio.apps.myrun.feature.routetracking.usecase

import akio.apps._base.error.UnauthorizedUserError
import akio.apps.myrun.data.activity.*
import akio.apps.myrun.data.authentication.UserAuthenticationState
import akio.apps.myrun.data.routetracking.RouteTrackingLocationRepository
import akio.apps.myrun.data.routetracking.RouteTrackingState
import akio.apps.myrun._base.utils.toGmsLatLng
import akio.apps.myrun.data.fitness.FitnessDataRepository
import akio.apps.myrun.data.fitness.SingleDataPoint
import akio.apps.myrun.data.location.LocationEntity
import akio.apps.myrun.data.userprofile.UserProfileRepository
import akio.apps.myrun.feature.routetracking.SaveRouteTrackingActivityUsecase
import akio.apps.myrun.feature.usertimeline.model.Activity
import akio.apps.myrun.feature.usertimeline.model.ActivityEntityMapper
import android.graphics.Bitmap
import com.google.maps.android.PolyUtil
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class SaveRouteTrackingActivityUsecaseImpl @Inject constructor(
    private val routeTrackingState: RouteTrackingState,
    private val routeTrackingLocationRepository: RouteTrackingLocationRepository,
    private val activityRepository: ActivityRepository,
    private val userAuthenticationState: UserAuthenticationState,
    private val userProfileRepository: UserProfileRepository,
    private val fitnessDataRepository: FitnessDataRepository,
    private val activityEntityMapper: ActivityEntityMapper
) : SaveRouteTrackingActivityUsecase {

    override suspend fun saveCurrentActivity(activityType: ActivityType, routeMapImage: Bitmap): Activity {
        val userAccountId = userAuthenticationState.getUserAccountId()
            ?: throw UnauthorizedUserError()

        val userProfile = userProfileRepository.getUserProfile(userAccountId)

        val endTime = System.currentTimeMillis()
        val startTime = routeTrackingState.getTrackingStartTime()
        val duration = routeTrackingState.getTrackingDuration()
        val distance = routeTrackingState.getRouteDistance()
        val trackedLocations = routeTrackingLocationRepository.getAllLocations()
        val encodedPolyline = PolyUtil.encode(trackedLocations.map { it.toGmsLatLng() })
        val activityData: ActivityEntity = ActivityDataEntity("", userProfile.accountId, userProfile.name, userProfile.photo, activityType, "", "", startTime, endTime, duration, distance, encodedPolyline)

        val speedDataPoints = fitnessDataRepository.getSpeedDataPoints(startTime, endTime, FITNESS_DATA_INTERVAL)
        val stepCadenceDataPoints: List<SingleDataPoint<Int>>? = if (activityType == ActivityType.Running) {
            fitnessDataRepository.getSteppingCadenceDataPoints(startTime, endTime, FITNESS_DATA_INTERVAL)
        } else {
            null
        }
        val locationDataPoints = trackedLocations.map { SingleDataPoint(it.time, LocationEntity(it.latitude, it.longitude, it.altitude)) }
        val savingActivity: ActivityEntity = when (activityType) {
            ActivityType.Running -> {
                val pace = (duration / (1000 * 60)) / (distance / 1000)
                RunningActivityEntity(activityData, pace)
            }

            ActivityType.Cycling -> {
                val speed = (distance / 1000) / (TimeUnit.MILLISECONDS.toHours(duration))
                CyclingActivityEntity(activityData, speed)
            }

            else -> throw UnsupportedOperationException("Saving unknown activity type $activityType")
        }

        val activityId = activityRepository.saveActivity(savingActivity, routeMapImage, speedDataPoints, stepCadenceDataPoints, locationDataPoints)

        return activityEntityMapper.map(activityId, savingActivity)
    }

    companion object {
        const val FITNESS_DATA_INTERVAL = 3000L
    }
}