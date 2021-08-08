package akio.apps.myrun.data.routetracking

import akio.apps.myrun.data.location.Location

interface RouteTrackingLocationRepository {
    suspend fun insert(trackingLocations: List<Location>)
    suspend fun clearRouteTrackingLocation()
    suspend fun getTrackedLocations(skip: Int): List<Location>
    suspend fun getLatestLocationTime(): Long
    suspend fun getAllLocations(): List<Location>
}
