package akio.apps.myrun.domain.tracking

import akio.apps.myrun.data.activity.api.ActivityLocalStorage
import akio.apps.myrun.data.activity.api.model.ActivityDataModel
import akio.apps.myrun.data.activity.api.model.ActivityLocation
import akio.apps.myrun.data.activity.api.model.ActivityModel
import akio.apps.myrun.data.activity.api.model.ActivityType
import akio.apps.myrun.data.activity.api.model.CyclingActivityModel
import akio.apps.myrun.data.activity.api.model.RunningActivityModel
import akio.apps.myrun.data.authentication.api.UserAuthenticationState
import akio.apps.myrun.data.eapps.api.ExternalAppProvidersRepository
import akio.apps.myrun.data.location.api.PolyUtil
import akio.apps.myrun.data.time.Now
import akio.apps.myrun.domain.activity.api.getLatLng
import akio.apps.myrun.domain.common.ObjectAutoId
import akio.apps.myrun.domain.common.TrackingValueConverter
import akio.apps.myrun.data.tracking.api.RouteTrackingLocationRepository
import akio.apps.myrun.data.tracking.api.RouteTrackingState
import akio.apps.myrun.wiring.common.NamedIoDispatcher
import android.graphics.Bitmap
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
    private val polyUtil: PolyUtil,
    @NamedIoDispatcher private val ioDispatcher: CoroutineDispatcher,
) {
    suspend operator fun invoke(activityName: String, routeImageBitmap: Bitmap) =
        withContext(ioDispatcher) {
            val userId = userAuthenticationState.requireUserAccountId()
            val activityId = objectAutoId.autoId()
            val activityLocations = routeTrackingLocationRepository.getAllLocations()
            val activityModel =
                createActivityInfo(userId, activityId, activityName, activityLocations)

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
        trackedLocations: List<ActivityLocation>,
    ): ActivityModel {
        val endTime = Now.currentTimeMillis()
        val startTime = routeTrackingState.getTrackingStartTime()
        val duration = routeTrackingState.getTrackingDuration()
        val distance = routeTrackingState.getRouteDistance()
        val encodedPolyline = polyUtil.encode(trackedLocations.map { it.getLatLng() })
        val placeIdentifier = routeTrackingState.getPlaceIdentifier()
        val activityType = routeTrackingState.getActivityType()
        val activityData = ActivityDataModel(
            activityId,
            activityType,
            activityName,
            routeImage = "", // local storage does not include
            placeIdentifier,
            startTime
                ?: endTime - duration,
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
