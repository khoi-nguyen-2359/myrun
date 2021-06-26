package akio.apps.myrun.data.location

import akio.apps.myrun.data.routetracking.model.LocationRequestConfig
import kotlinx.coroutines.flow.Flow

interface LocationDataSource {
    suspend fun getLastLocation(): LocationEntity?
    fun getLocationUpdate(request: LocationRequestConfig): Flow<List<LocationEntity>>
}
