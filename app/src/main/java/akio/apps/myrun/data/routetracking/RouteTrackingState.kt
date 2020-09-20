package akio.apps.myrun.data.routetracking

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
}