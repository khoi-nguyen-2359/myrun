package akio.apps.myrun.data.tracking.impl

import akio.apps.myrun.data.activity.api.model.ActivityLocation
import akio.apps.myrun.data.tracking.api.RouteTrackingLocationRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RouteTrackingLocationRepositoryImpl @Inject constructor(
    private val routeTrackingLocationDao: RouteTrackingLocationDao,
) : RouteTrackingLocationRepository {

    override suspend fun insert(trackingLocations: List<ActivityLocation>): Unit =
        routeTrackingLocationDao.insert(trackingLocations.map { it.toRoomEntity(0) })

    override suspend fun clearRouteTrackingLocation() {
        routeTrackingLocationDao.clear()
    }

    override suspend fun getTrackedLocations(skip: Int): List<ActivityLocation> =
        routeTrackingLocationDao.getLocations(skip)
            .map { it.toLocation() }

    override suspend fun getAllLocations(): List<ActivityLocation> =
        routeTrackingLocationDao.getAll()
            .map { roomLocation ->
                roomLocation.toLocation()
            }

    override suspend fun getLatestLocationTime(): Long =
        routeTrackingLocationDao.getLatestLocationTime()

    private fun ActivityLocation.toRoomEntity(id: Int) =
        RoomTrackingLocation(id, elapsedTime, latitude, longitude, altitude, speed)

    private fun RoomTrackingLocation.toLocation() =
        ActivityLocation(time, latitude, longitude, altitude, speed)
}
