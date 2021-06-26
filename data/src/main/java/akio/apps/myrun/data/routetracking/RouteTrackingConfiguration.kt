package akio.apps.myrun.data.routetracking

import akio.apps.myrun.data.routetracking.model.LocationRequestConfig
import kotlinx.coroutines.flow.Flow

interface RouteTrackingConfiguration {
    fun getLocationRequestConfig(): Flow<LocationRequestConfig>
    suspend fun setLocationRequestConfiguration(config: LocationRequestConfig)
    suspend fun setLocationAccumulationEnabled(isEnabled: Boolean)
    fun isLocationAccumulationEnabled(): Flow<Boolean>
}
