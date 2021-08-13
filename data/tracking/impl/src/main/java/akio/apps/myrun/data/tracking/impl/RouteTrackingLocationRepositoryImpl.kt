package akio.apps.myrun.data.tracking.impl

import akio.apps.common.wiring.NamedIoDispatcher
import akio.apps.myrun.data.location.api.model.Location
import akio.apps.myrun.data.tracking.api.RouteTrackingLocationRepository
import javax.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

class RouteTrackingLocationRepositoryImpl @Inject constructor(
    private val routeTrackingLocationDao: RouteTrackingLocationDao,
    @NamedIoDispatcher private val ioDispatcher: CoroutineDispatcher
) : RouteTrackingLocationRepository {

    override suspend fun insert(trackingLocations: List<Location>): Unit =
        withContext(ioDispatcher) {
            routeTrackingLocationDao.insert(trackingLocations.map { it.toRoomEntity(0) })
        }

    override suspend fun clearRouteTrackingLocation(): Unit = withContext(ioDispatcher) {
        routeTrackingLocationDao.clear()
    }

    override suspend fun getTrackedLocations(skip: Int): List<Location> =
        routeTrackingLocationDao.getLocations(skip)
            .map { it.toLocation() }

    override suspend fun getAllLocations(): List<Location> =
        routeTrackingLocationDao.getAll()
            .map { roomLocation ->
                roomLocation.toLocation()
            }

    override suspend fun getLatestLocationTime(): Long = withContext(ioDispatcher) {
        routeTrackingLocationDao.getLatestLocationTime()
    }

    private fun Location.toRoomEntity(id: Int) =
        RoomTrackingLocation(id, time, latitude, longitude, altitude, speed)

    private fun RoomTrackingLocation.toLocation() =
        Location(time, latitude, longitude, altitude, speed)
}
