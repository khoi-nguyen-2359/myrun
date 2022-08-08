package akio.apps.myrun.data.tracking.api

import akio.apps.myrun.data.activity.api.model.ActivityType
import akio.apps.myrun.data.location.api.model.Location
import akio.apps.myrun.data.tracking.api.model.RouteTrackingStatus
import akio.apps.myrun.data.user.api.model.PlaceIdentifier
import kotlinx.coroutines.flow.Flow

interface RouteTrackingState {
    suspend fun getTrackingStatus(): @RouteTrackingStatus Int
    fun getTrackingStatusFlow(): Flow<@RouteTrackingStatus Int>
    suspend fun setTrackingStatus(@RouteTrackingStatus status: Int)
    suspend fun setRouteDistance(distance: Double)
    suspend fun getRouteDistance(): Double
    suspend fun setTrackingStartTime(startTime: Long)
    suspend fun getTrackingStartTime(): Long?
    suspend fun setInstantSpeed(currentSpeed: Double)
    suspend fun getInstantSpeed(): Double

    suspend fun setTrackingDuration(totalSec: Long)
    suspend fun getTrackingDuration(): Long
    suspend fun setLastPauseTime(checkPointMs: Long?)
    suspend fun getLastPauseTime(): Long?
    suspend fun setPauseDuration(duration: Long)
    suspend fun getPauseDuration(): Long

    suspend fun getActivityType(): ActivityType
    suspend fun setActivityType(activityType: ActivityType)

    suspend fun setStartLocation(location: Location)
    suspend fun getStartLocation(): Location?

    /**
     * Calendar time of the last location update during tracking. This value is used for
     * calculating the pause duration if tracking is interrupted (process is killed).
     */
    suspend fun setLastLocationUpdateTime(time: Long)
    suspend fun getLastLocationUpdateTime(): Long?

    suspend fun setPlaceIdentifier(placeIdentifier: PlaceIdentifier)
    suspend fun getPlaceIdentifier(): PlaceIdentifier?

    suspend fun clear()
}
