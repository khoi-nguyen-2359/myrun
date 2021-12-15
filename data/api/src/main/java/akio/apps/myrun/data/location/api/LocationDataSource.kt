package akio.apps.myrun.data.location.api

import akio.apps.myrun.data.location.api.model.Location
import akio.apps.myrun.data.location.api.model.LocationRequestConfig
import kotlinx.coroutines.flow.Flow

const val LOG_TAG_LOCATION = "LOCATION"

interface LocationDataSource {
    suspend fun getLastLocation(): Location?

    /**
     * Returns a flow of last locations that will try to emit a non-null location right after it is
     * ready.
     */
    fun getLastLocationFlow(): Flow<Location>
    fun getLocationUpdate(request: LocationRequestConfig): Flow<List<Location>>
}
