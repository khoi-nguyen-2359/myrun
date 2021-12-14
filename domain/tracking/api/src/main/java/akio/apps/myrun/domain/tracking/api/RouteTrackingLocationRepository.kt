package akio.apps.myrun.domain.tracking.api

import akio.apps.myrun.domain.activity.api.model.ActivityLocation

interface RouteTrackingLocationRepository {
    suspend fun insert(trackingLocations: List<ActivityLocation>)
    suspend fun clearRouteTrackingLocation()
    suspend fun getTrackedLocations(skip: Int): List<ActivityLocation>
    suspend fun getLatestLocationTime(): Long
    suspend fun getAllLocations(): List<ActivityLocation>
}
