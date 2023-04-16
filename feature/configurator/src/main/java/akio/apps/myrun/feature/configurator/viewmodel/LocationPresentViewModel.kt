package akio.apps.myrun.feature.configurator.viewmodel

import akio.apps.myrun.data.tracking.api.LocationPresentConfiguration
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class LocationPresentViewModel @Inject constructor(
    private val locationPresentConfiguration: LocationPresentConfiguration,
) : ViewModel() {
    val isBSplinesEnabledFlow: Flow<Boolean> = locationPresentConfiguration.isBSplinesEnabledFlow()

    fun updateConfig(isBSPlinesEnabled: Boolean) = viewModelScope.launch {
        locationPresentConfiguration.setBSplinesEnabled(isBSPlinesEnabled)
    }
}
