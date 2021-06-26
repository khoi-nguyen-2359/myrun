package akio.apps.myrun.data.routetracking

import akio.apps.myrun.data.routetracking.model.LocationRequestConfig

interface RouteTrackingConfiguration {
    suspend fun getLocationRequestConfig(): LocationRequestConfig
    suspend fun setLocationRequestInfo(config: LocationRequestConfig)
    suspend fun setLocationAccumulationEnabled(isEnabled: Boolean)
    suspend fun isLocationAccumulationEnabled(): Boolean
}
