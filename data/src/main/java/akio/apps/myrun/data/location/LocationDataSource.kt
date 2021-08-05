package akio.apps.myrun.data.location

import akio.apps.myrun.data.routetracking.model.LocationRequestConfig
import kotlinx.coroutines.flow.Flow

interface LocationDataSource {
    suspend fun getLastLocation(): Location?

    /**
     * Returns a flow of last locations that will try to emit a non-null location right after it is
     * ready.
     */
    fun getLastLocationFlow(): Flow<Location>
    fun getLocationUpdate(request: LocationRequestConfig): Flow<List<Location>>
}
