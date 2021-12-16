package akio.apps.myrun.data.tracking.api

import akio.apps.myrun.data.activity.api.model.ActivityLocation

interface RouteTrackingLocationRepository {
    suspend fun insert(trackingLocations: List<ActivityLocation>)
    suspend fun clearRouteTrackingLocation()
    suspend fun getTrackedLocations(skip: Int): List<ActivityLocation>
    suspend fun getLatestLocationTime(): Long
    suspend fun getAllLocations(): List<ActivityLocation>
}
