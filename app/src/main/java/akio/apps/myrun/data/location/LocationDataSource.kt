package akio.apps.myrun.data.location

import android.location.Location
import kotlinx.coroutines.flow.Flow

interface LocationDataSource {
    suspend fun getLastLocation(): Location?
    fun getLocationUpdate(locationRequest: LocationRequestEntity): Flow<List<Location>>
}