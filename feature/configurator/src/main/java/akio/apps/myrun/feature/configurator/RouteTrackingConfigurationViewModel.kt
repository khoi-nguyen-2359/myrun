package akio.apps.myrun.feature.configurator

import akio.apps.myrun.data.location.api.model.LocationRequestConfig
import akio.apps.myrun.data.tracking.api.RouteTrackingConfiguration
import akio.apps.myrun.data.tracking.api.model.LocationProcessingConfig
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class RouteTrackingConfigurationViewModel @Inject constructor(
    private val routeTrackingConfiguration: RouteTrackingConfiguration,
) : ViewModel() {

    // This value is for rolling back when text input is not a number
    private var cachedLocationRequestConfig: LocationRequestConfig = LocationRequestConfig()
    private var locationUpdateConfigState: MutableStateFlow<LocationUpdateConfiguration> =
        MutableStateFlow(LocationUpdateConfiguration())

    val locationUpdateConfigFlow: Flow<LocationUpdateConfiguration> = locationUpdateConfigState

    init {
        routeTrackingConfiguration.getLocationRequestConfigFlow()
            .combine(
                routeTrackingConfiguration.getLocationProcessingConfig()
            ) { locationRequestConfig, locationProcessingConfig ->
                cachedLocationRequestConfig = locationRequestConfig
                LocationUpdateConfiguration(
                    updateInterval = locationRequestConfig.updateInterval.toString(),
                    fastestUpdateInterval = locationRequestConfig.fastestUpdateInterval.toString(),
                    smallestDisplacement = locationRequestConfig.smallestDisplacement.toString(),
                    isAvgAccumulationEnabled = locationProcessingConfig.isAvgAccumulatorEnabled,
                    isSpeedFilterEnabled = locationProcessingConfig.isSpeedFilterEnabled
                )
            }
            .onEach { locationUpdateConfigState.value = it }
            .launchIn(viewModelScope)
    }

    fun onLocationUpdateConfigurationChanged(
        updateConfig: LocationUpdateConfiguration,
    ) = viewModelScope.launch {
        locationUpdateConfigState.value = updateConfig
    }

    fun applyChanges() = viewModelScope.launch {
        val updateConfig = locationUpdateConfigState.value

        val updateLocationRequest = LocationRequestConfig(
            updateInterval = updateConfig.updateInterval.toLongOrNull()
                ?: cachedLocationRequestConfig.updateInterval,
            fastestUpdateInterval = updateConfig.fastestUpdateInterval.toLongOrNull()
                ?: cachedLocationRequestConfig.fastestUpdateInterval,
            smallestDisplacement = updateConfig.smallestDisplacement.toFloatOrNull()
                ?: cachedLocationRequestConfig.smallestDisplacement
        )
        routeTrackingConfiguration.setLocationRequestConfiguration(updateLocationRequest)

        routeTrackingConfiguration.setLocationProcessingConfig(
            LocationProcessingConfig(
                updateConfig.isAvgAccumulationEnabled,
                updateConfig.isSpeedFilterEnabled
            )
        )
    }

    data class LocationUpdateConfiguration(
        val updateInterval: String = "",
        val fastestUpdateInterval: String = "",
        val smallestDisplacement: String = "",
        val isAvgAccumulationEnabled: Boolean = false,
        val isSpeedFilterEnabled: Boolean = false,
    )
}
