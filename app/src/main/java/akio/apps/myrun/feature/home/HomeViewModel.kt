package akio.apps.myrun.feature.home

import akio.apps.myrun.data.routetracking.RouteTrackingConfiguration
import akio.apps.myrun.data.routetracking.model.LocationRequestConfig
import androidx.lifecycle.ViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.first

class HomeViewModel @Inject constructor(
    private val routeTrackingConfiguration: RouteTrackingConfiguration
) : ViewModel() {
    suspend fun getLocationRequest(): LocationRequestConfig =
        routeTrackingConfiguration.getLocationRequestConfig().first()
}
