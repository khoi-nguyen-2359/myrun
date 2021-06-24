package akio.apps.myrun.data.location

import akio.apps.myrun.data.routetracking.model.LocationRequestConfig
import android.location.Location
import kotlinx.coroutines.flow.Flow

interface LocationDataSource {
    suspend fun getLastLocation(): Location?
    fun getLocationUpdate(locationRequest: LocationRequestConfig): Flow<List<Location>>
}
