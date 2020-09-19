package akio.apps.myrun.data.routetracking

import akio.apps.myrun.data.routetracking.model.TrackingLocationEntity
import android.location.Location

interface RouteTrackingLocationRepository {
    suspend fun insert(trackingLocations: List<Location>)
    suspend fun clearRouteTrackingLocation()
    suspend fun getRouteTrackingLocationUpdates(skip: Int): List<TrackingLocationEntity>
    suspend fun getLatestLocationTime(): Long
    suspend fun getAllLocations(): List<TrackingLocationEntity>
}