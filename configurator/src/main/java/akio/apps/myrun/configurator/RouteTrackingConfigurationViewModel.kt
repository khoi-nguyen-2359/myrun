package akio.apps.myrun.configurator

import akio.apps.myrun.data.routetracking.RouteTrackingConfiguration
import akio.apps.myrun.data.routetracking.model.LocationRequestConfig
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class RouteTrackingConfigurationViewModel @Inject constructor(
    private val routeTrackingConfiguration: RouteTrackingConfiguration
) : ViewModel() {

    private val locationRequestConfigMutableState: MutableStateFlow<LocationRequestConfig> =
        MutableStateFlow(LocationRequestConfig.foo())

    init {
        initData()
    }

    suspend fun getLocationRequestConfig(): LocationRequestConfig =
        routeTrackingConfiguration.getLocationRequestConfig()

    private fun initData() = viewModelScope.launch {
        locationRequestConfigMutableState.value =
            routeTrackingConfiguration.getLocationRequestConfig()
    }

    fun setLocationRequestInfo(requestConfig: LocationRequestConfig) = viewModelScope.launch {
        routeTrackingConfiguration.setLocationRequestInfo(requestConfig)
        initData()
    }
}
