package akio.apps.myrun.domain.tracking.api

import akio.apps.myrun.data.location.api.model.LocationRequestConfig
import akio.apps.myrun.domain.tracking.api.model.LocationProcessingConfig
import kotlinx.coroutines.flow.Flow

interface RouteTrackingConfiguration {
    fun getLocationRequestConfig(): Flow<LocationRequestConfig>
    suspend fun setLocationRequestConfiguration(config: LocationRequestConfig)
    suspend fun setLocationProcessingConfig(config: LocationProcessingConfig)
    fun getLocationProcessingConfig(): Flow<LocationProcessingConfig>
}
