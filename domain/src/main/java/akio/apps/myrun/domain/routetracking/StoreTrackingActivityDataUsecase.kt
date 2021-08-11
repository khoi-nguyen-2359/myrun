package akio.apps.myrun.domain.routetracking

import akio.apps._base.ObjectAutoId
import akio.apps.common.wiring.NamedIoDispatcher
import akio.apps.myrun._base.utils.toGmsLatLng
import akio.apps.myrun.data.activity.api.ActivityLocalStorage
import akio.apps.myrun.data.activity.api.model.ActivityDataModel
import akio.apps.myrun.data.activity.api.model.ActivityLocation
import akio.apps.myrun.data.activity.api.model.ActivityModel
import akio.apps.myrun.data.activity.api.model.ActivityType
import akio.apps.myrun.data.activity.api.model.CyclingActivityModel
import akio.apps.myrun.data.activity.api.model.RunningActivityModel
import akio.apps.myrun.data.authentication.UserAuthenticationState
import akio.apps.myrun.data.externalapp.ExternalAppProvidersRepository
import akio.apps.myrun.data.tracking.api.RouteTrackingLocationRepository
import akio.apps.myrun.data.tracking.api.RouteTrackingState
import akio.apps.myrun.domain.TrackingValueConverter
import android.graphics.Bitmap
import com.google.maps.android.PolyUtil
import javax.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext

/**
 * Stores all information of an activity that have been tracked. Must not include any data that get
 * from server because this works in offline mode.
 */
class StoreTrackingActivityDataUsecase @Inject constructor(
    private val userAuthenticationState: UserAuthenticationState,
    private val routeTrackingState: RouteTrackingState,
    private val routeTrackingLocationRepository: RouteTrackingLocationRepository,
    private val activityLocalStorage: ActivityLocalStorage,
    private val externalAppProvidersRepository: ExternalAppProvidersRepository,
    private val objectAutoId: ObjectAutoId,
    @NamedIoDispatcher private val ioDispatcher: CoroutineDispatcher
) {
    suspend operator fun invoke(activityName: String, routeImageBitmap: Bitmap) =
        withContext(ioDispatcher) {
            val userId = userAuthenticationState.requireUserAccountId()
            val activityId = objectAutoId.autoId()
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
                if (externalAppProvidersRepository.isStravaSyncEnabled()) {
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
        trackedLocations: List<akio.apps.myrun.data.location.api.model.Location>
    ): ActivityModel {
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
            routeImage = "", // local storage does not include
            placeIdentifier,
            startTime,
            endTime,
            duration,
            distance,
            encodedPolyline,
            ActivityModel.AthleteInfo(userId = userId) // local storage does not include
        )
        return when (activityType) {
            ActivityType.Running -> {
                val pace =
                    TrackingValueConverter.TimeMinute.fromRawValue(duration) /
                        TrackingValueConverter.DistanceKm.fromRawValue(distance)
                RunningActivityModel(activityData, pace, cadence = 0)
            }
            ActivityType.Cycling -> {
                val speed =
                    TrackingValueConverter.DistanceKm.fromRawValue(distance) /
                        TrackingValueConverter.TimeHour.fromRawValue(duration)
                CyclingActivityModel(activityData, speed)
            }
            else -> throw UnsupportedOperationException(
                "Saving unknown activity type $activityType"
            )
        }
    }
}
