package akio.apps.myrun.data.routetracking.impl

import akio.apps.myrun.data.routetracking.RouteTrackingLocationRepository
import akio.apps.myrun.data.routetracking.dto.RoomTrackingLocation
import android.location.Location
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class RouteTrackingLocationRepositoryImpl @Inject constructor(
    private val routeTrackingLocationDao: RouteTrackingLocationDao
) : RouteTrackingLocationRepository {

    override suspend fun insert(trackingLocations: List<Location>): Unit = withContext(Dispatchers.IO) {
        routeTrackingLocationDao.insert(trackingLocations.map { it.toRoomEntity(0) })
    }

    override suspend fun clearRouteTrackingLocation(): Unit = withContext(Dispatchers.IO) {
        routeTrackingLocationDao.clear()
    }

    private fun Location.toRoomEntity(id: Int) = RoomTrackingLocation(id, time, latitude, longitude, altitude)

}