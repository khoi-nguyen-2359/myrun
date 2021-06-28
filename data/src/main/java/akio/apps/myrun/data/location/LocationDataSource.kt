package akio.apps.myrun.data.location

import akio.apps.myrun.data.routetracking.model.LocationRequestConfig
import kotlinx.coroutines.flow.Flow

interface LocationDataSource {
    suspend fun getLastLocation(): LocationEntity?

    /**
     * Returns a flow of last locations that will try to emit a non-null location right after it is
     * ready.
     */
    fun getLastLocationFlow(): Flow<LocationEntity>
    fun getLocationUpdate(request: LocationRequestConfig): Flow<List<LocationEntity>>
}
