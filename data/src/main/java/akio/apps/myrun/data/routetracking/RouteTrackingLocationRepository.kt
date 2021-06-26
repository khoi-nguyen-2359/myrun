package akio.apps.myrun.data.routetracking

import akio.apps.myrun.data.location.LocationEntity

interface RouteTrackingLocationRepository {
    suspend fun insert(trackingLocations: List<LocationEntity>)
    suspend fun clearRouteTrackingLocation()
    suspend fun getTrackedLocations(skip: Int): List<LocationEntity>
    suspend fun getLatestLocationTime(): Long
    suspend fun getAllLocations(): List<LocationEntity>
}
