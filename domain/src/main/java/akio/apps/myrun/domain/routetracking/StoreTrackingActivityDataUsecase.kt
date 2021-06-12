package akio.apps.myrun.domain.routetracking

import akio.apps.myrun._base.utils.toGmsLatLng
import akio.apps.myrun._di.NamedIoDispatcher
import akio.apps.myrun.data.activity.ActivityRepository
import akio.apps.myrun.data.activity.model.ActivityDataModel
import akio.apps.myrun.data.activity.model.ActivityModel
import akio.apps.myrun.data.activity.model.ActivityType
import akio.apps.myrun.data.activity.model.CyclingActivityModel
import akio.apps.myrun.data.activity.model.RunningActivityModel
import akio.apps.myrun.data.activitysharing.ActivityLocalStorage
import akio.apps.myrun.data.activitysharing.model.ActivityLocation
import akio.apps.myrun.data.authentication.UserAuthenticationState
import akio.apps.myrun.data.externalapp.ExternalAppProvidersRepository
import akio.apps.myrun.data.routetracking.RouteTrackingLocationRepository
import akio.apps.myrun.data.routetracking.RouteTrackingState
import akio.apps.myrun.data.routetracking.TrackingLocationEntity
import akio.apps.myrun.data.userprofile.UserProfileRepository
import akio.apps.myrun.domain.PerformanceUnit
import android.graphics.Bitmap
import com.google.maps.android.PolyUtil
import javax.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext

class StoreTrackingActivityDataUsecase @Inject constructor(
    private val activityRepository: ActivityRepository,
    private val userAuthenticationState: UserAuthenticationState,
    private val routeTrackingState: RouteTrackingState,
    private val userProfileRepository: UserProfileRepository,
    private val routeTrackingLocationRepository: RouteTrackingLocationRepository,
    private val activityLocalStorage: ActivityLocalStorage,
    private val externalAppProvidersRepository: ExternalAppProvidersRepository,
    @NamedIoDispatcher private val ioDispatcher: CoroutineDispatcher
) {
    suspend operator fun invoke(activityName: String, routeImageBitmap: Bitmap) =
        withContext(ioDispatcher) {
            val userId = userAuthenticationState.getUserAccountId() ?: return@withContext
            val activityId = activityRepository.generateActivityId(userId)
            val trackedLocations = routeTrackingLocationRepository.getAllLocations()
            val activityModel =
                createActivityInfo(userId, activityId, activityName, trackedLocations)

            val activityLocations = trackedLocations.map {
                ActivityLocation(activityId, it.time, it.latitude, it.longitude, it.altitude)
            }

            val activityStorageAsync = async {
                activityLocalStorage.storeActivityData(
                    activityModel,
                    activityLocations,
                    routeImageBitmap
                )
            }

            val activitySyncAsync = async {
                if (externalAppProvidersRepository.getStravaProviderToken(userId) != null) {
                    activityLocalStorage.storeActivitySyncData(activityModel, activityLocations)
                }
            }
            activityStorageAsync.join()
            activitySyncAsync.join()
        }

    private suspend fun createActivityInfo(
        userId: String,
        activityId: String,
        activityName: String,
        trackedLocations: List<TrackingLocationEntity>
    ): ActivityModel {
        val userProfile = userProfileRepository.getUserProfile(userId)
        val endTime = System.currentTimeMillis()
        val startTime = routeTrackingState.getTrackingStartTime()
        val duration = routeTrackingState.getTrackingDuration()
        val distance = routeTrackingState.getRouteDistance()
        val encodedPolyline = PolyUtil.encode(trackedLocations.map { it.toGmsLatLng() })
        val placeIdentifier = routeTrackingState.getPlaceIdentifier()
        val activityType = routeTrackingState.getActivityType()
        val activityData = ActivityDataModel(
            activityId,
            activityType,
            activityName,
            routeImage = "",    // not yet stored
            placeIdentifier,
            startTime,
            endTime,
            duration,
            distance,
            encodedPolyline,
            ActivityModel.AthleteInfo(
                userProfile.accountId,
                userProfile.name,
                userProfile.photo,
            )
        )
        return when (activityType) {
            ActivityType.Running -> {
                val pace =
                    PerformanceUnit.TimeMinute.fromRawValue(duration) /
                        PerformanceUnit.DistanceKm.fromRawValue(distance)
                RunningActivityModel(activityData, pace, cadence = 0)
            }
            ActivityType.Cycling -> {
                val speed =
                    PerformanceUnit.DistanceKm.fromRawValue(distance) /
                        PerformanceUnit.TimeHour.fromRawValue(duration)
                CyclingActivityModel(activityData, speed)
            }
            else -> throw UnsupportedOperationException(
                "Saving unknown activity type $activityType"
            )
        }
    }
}
