package akio.apps.myrun.data.routetracking.impl

import akio.apps.myrun.data.routetracking.RouteTrackingLocationRepository
import akio.apps.myrun.data.routetracking.dto.RoomTrackingLocation
import android.location.Location
import javax.inject.Inject

class RouteTrackingLocationRepositoryImpl @Inject constructor(
    private val routeTrackingLocationDao: RouteTrackingLocationDao
) : RouteTrackingLocationRepository {

    override fun insert(trackingLocations: List<Location>) {
        routeTrackingLocationDao.insert(trackingLocations.map { it.toRoomEntity(0) })
    }

    private fun Location.toRoomEntity(id: Int) = RoomTrackingLocation(id, time, latitude, longitude, altitude)

}