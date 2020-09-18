package akio.apps.myrun.data.routetracking

import akio.apps.myrun.data.routetracking.dto.TrackingLocationEntity
import android.location.Location
import kotlinx.coroutines.flow.Flow

interface RouteTrackingLocationRepository {
    suspend fun insert(trackingLocations: List<Location>)
    suspend fun clearRouteTrackingLocation()
    fun getRouteTrackingLocationUpdates(skip: Int): Flow<List<TrackingLocationEntity>>
    suspend fun getLatestLocationTime(): Long
}