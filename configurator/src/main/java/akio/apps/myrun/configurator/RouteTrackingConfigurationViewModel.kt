package akio.apps.myrun.configurator

import akio.apps.myrun.data.routetracking.RouteTrackingConfiguration
import akio.apps.myrun.data.routetracking.model.LocationRequestConfig
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
    private val routeTrackingConfiguration: RouteTrackingConfiguration
) : ViewModel() {

    // This value is for rolling back when text input is not a number
    private var cacheLocationRequestConfig: LocationRequestConfig = LocationRequestConfig.foo()
    private var locationUpdateConfigState: MutableStateFlow<LocationUpdateConfiguration> =
        MutableStateFlow(LocationUpdateConfiguration())
    val locationUpdateConfigFlow: Flow<LocationUpdateConfiguration> = locationUpdateConfigState

    init {
        routeTrackingConfiguration.getLocationRequestConfig()
            .onEach { cacheLocationRequestConfig = it }
            .combine(routeTrackingConfiguration.isLocationAccumulationEnabled()) {
                locationRequestConfig, isLocationAccumulationEnabled ->
                LocationUpdateConfiguration(
                    updateInterval = locationRequestConfig.updateInterval.toString(),
                    fastestUpdateInterval = locationRequestConfig.fastestUpdateInterval.toString(),
                    smallestDisplacement = locationRequestConfig.smallestDisplacement.toString(),
                    isAccumulationEnabled = isLocationAccumulationEnabled
                )
            }
            .onEach { locationUpdateConfigState.value = it }
            .launchIn(viewModelScope)
    }

    fun onLocationUpdateConfigurationChanged(
        updateConfig: LocationUpdateConfiguration
    ) = viewModelScope.launch {
        locationUpdateConfigState.value = updateConfig
    }

    fun applyChanges() = viewModelScope.launch {
        val updateConfig = locationUpdateConfigState.value
        val updateLocationRequest = LocationRequestConfig(
            updateInterval = updateConfig.updateInterval.toLongOrNull()
                ?: cacheLocationRequestConfig.updateInterval,
            fastestUpdateInterval = updateConfig.fastestUpdateInterval.toLongOrNull()
                ?: cacheLocationRequestConfig.fastestUpdateInterval,
            smallestDisplacement = updateConfig.smallestDisplacement.toFloatOrNull()
                ?: cacheLocationRequestConfig.smallestDisplacement
        )
        routeTrackingConfiguration.setLocationRequestConfiguration(updateLocationRequest)
        routeTrackingConfiguration.setLocationAccumulationEnabled(
            updateConfig.isAccumulationEnabled
        )
    }

    data class LocationUpdateConfiguration(
        val updateInterval: String = "",
        val fastestUpdateInterval: String = "",
        val smallestDisplacement: String = "",
        val isAccumulationEnabled: Boolean = false
    )
}
