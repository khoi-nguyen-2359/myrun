package akio.apps.myrun.data.tracking.api

import akio.apps.myrun.data.location.api.model.LocationRequestConfig
import akio.apps.myrun.data.tracking.api.model.LocationProcessingConfig
import kotlinx.coroutines.flow.Flow

interface RouteTrackingConfiguration {
    suspend fun getLocationRequestConfig(): LocationRequestConfig
    fun getLocationRequestConfigFlow(): Flow<LocationRequestConfig>
    suspend fun setLocationRequestConfiguration(config: LocationRequestConfig)
    suspend fun setLocationProcessingConfig(config: LocationProcessingConfig)
    fun getLocationProcessingConfig(): Flow<LocationProcessingConfig>
}
