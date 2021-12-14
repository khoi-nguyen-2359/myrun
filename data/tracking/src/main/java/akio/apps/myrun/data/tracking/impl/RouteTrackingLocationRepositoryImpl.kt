package akio.apps.myrun.data.tracking.impl

import akio.apps.myrun.domain.tracking.api.RouteTrackingLocationRepository
import akio.apps.myrun.data.wiring.NamedIoDispatcher
import akio.apps.myrun.domain.activity.api.model.ActivityLocation
import javax.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

class RouteTrackingLocationRepositoryImpl @Inject constructor(
    private val routeTrackingLocationDao: RouteTrackingLocationDao,
    @NamedIoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : RouteTrackingLocationRepository {

    override suspend fun insert(trackingLocations: List<ActivityLocation>): Unit =
        withContext(ioDispatcher) {
            routeTrackingLocationDao.insert(trackingLocations.map { it.toRoomEntity(0) })
        }

    override suspend fun clearRouteTrackingLocation(): Unit = withContext(ioDispatcher) {
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

    override suspend fun getLatestLocationTime(): Long = withContext(ioDispatcher) {
        routeTrackingLocationDao.getLatestLocationTime()
    }

    private fun ActivityLocation.toRoomEntity(id: Int) =
        RoomTrackingLocation(id, elapsedTime, latitude, longitude, altitude, speed)

    private fun RoomTrackingLocation.toLocation() =
        ActivityLocation(time, latitude, longitude, altitude, speed)
}
