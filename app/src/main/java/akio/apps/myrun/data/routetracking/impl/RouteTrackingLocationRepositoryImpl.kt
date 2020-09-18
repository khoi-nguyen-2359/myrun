package akio.apps.myrun.data.routetracking.impl

import akio.apps.myrun.data.routetracking.RouteTrackingLocationRepository
import akio.apps.myrun.data.routetracking.dto.RoomTrackingLocation
import akio.apps.myrun.data.routetracking.dto.TrackingLocationEntity
import android.location.Location
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject

class RouteTrackingLocationRepositoryImpl @Inject constructor(
    private val routeTrackingLocationDao: RouteTrackingLocationDao
) : RouteTrackingLocationRepository {

    override suspend fun insert(trackingLocations: List<Location>): Unit = withContext(Dispatchers.IO) {
        Timber.d("insert tracking location ${trackingLocations.size}")
        routeTrackingLocationDao.insert(trackingLocations.map { it.toRoomEntity(0) })
    }

    override suspend fun clearRouteTrackingLocation(): Unit = withContext(Dispatchers.IO) {
        routeTrackingLocationDao.clear()
    }

    override fun getRouteTrackingLocationUpdates(skip: Int): Flow<List<TrackingLocationEntity>> = routeTrackingLocationDao.getLocations(skip).map { roomLocations ->
        Timber.d("repo: get tracking location update ${roomLocations.size}")
        roomLocations.map { it.toTrackingLocationEntity() }
    }
        .onCompletion {
            Timber.d("onCompletion")
        }
        .flowOn(Dispatchers.IO)

    override suspend fun getLatestLocationTime(): Long = withContext(Dispatchers.IO) {
        routeTrackingLocationDao.getLatestLocationTime()
    }

    private fun Location.toRoomEntity(id: Int) = RoomTrackingLocation(id, time, latitude, longitude, altitude)

    private fun RoomTrackingLocation.toTrackingLocationEntity() = TrackingLocationEntity(time, latitude, longitude, altitude)
}