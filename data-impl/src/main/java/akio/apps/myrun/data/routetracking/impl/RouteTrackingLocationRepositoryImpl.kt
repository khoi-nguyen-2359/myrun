package akio.apps.myrun.data.routetracking.impl

import akio.apps.myrun._di.NamedIoDispatcher
import akio.apps.myrun.data.location.LocationEntity
import akio.apps.myrun.data.routetracking.RouteTrackingLocationRepository
import javax.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

class RouteTrackingLocationRepositoryImpl @Inject constructor(
    private val routeTrackingLocationDao: RouteTrackingLocationDao,
    @NamedIoDispatcher private val ioDispatcher: CoroutineDispatcher
) : RouteTrackingLocationRepository {

    override suspend fun insert(trackingLocations: List<LocationEntity>): Unit =
        withContext(ioDispatcher) {
            routeTrackingLocationDao.insert(trackingLocations.map { it.toRoomEntity(0) })
        }

    override suspend fun clearRouteTrackingLocation(): Unit = withContext(ioDispatcher) {
        routeTrackingLocationDao.clear()
    }

    override suspend fun getTrackedLocations(skip: Int): List<LocationEntity> =
        routeTrackingLocationDao.getLocations(skip)
            .map { it.toLocationEntity() }

    override suspend fun getAllLocations(): List<LocationEntity> =
        routeTrackingLocationDao.getAll()
            .map { roomLocation ->
                roomLocation.toLocationEntity()
            }

    override suspend fun getLatestLocationTime(): Long = withContext(ioDispatcher) {
        routeTrackingLocationDao.getLatestLocationTime()
    }

    private fun LocationEntity.toRoomEntity(id: Int) =
        RoomTrackingLocation(id, time, latitude, longitude, altitude, speed)

    private fun RoomTrackingLocation.toLocationEntity() =
        LocationEntity(time, latitude, longitude, altitude, speed)
}
