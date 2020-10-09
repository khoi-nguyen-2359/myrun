package akio.apps.myrun.data.routetracking

import akio.apps.myrun.data.activity.ActivityType
import akio.apps.myrun.data.location.LatLngEntity
import android.location.Location
import kotlinx.coroutines.flow.Flow

interface RouteTrackingState {
    suspend fun getTrackingStatus(): @RouteTrackingStatus Int
    fun getTrackingStatusFlow(): Flow<@RouteTrackingStatus Int>
    suspend fun setTrackingStatus(@RouteTrackingStatus status: Int)
    suspend fun setRouteDistance(distance: Double)
    suspend fun getRouteDistance(): Double
    suspend fun setTrackingStartTime(startTime: Long)
    suspend fun getTrackingStartTime(): Long
    suspend fun setInstantSpeed(currentSpeed: Double)
    suspend fun getInstantSpeed(): Double
    suspend fun setTrackingDuration(totalSec: Long)
    suspend fun getTrackingDuration(): Long
    suspend fun setLastResumeTime(resumeTime: Long)
    suspend fun getLastResumeTime(): Long
    suspend fun clear()
    suspend fun getActivityType(): ActivityType
    suspend fun setActivityType(activityType: ActivityType)
    suspend fun setStartLocation(latLng: LatLngEntity)
    suspend fun getStartLocation(): LatLngEntity?
}