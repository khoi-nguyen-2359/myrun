package akio.apps.myrun.feature.main

import akio.apps.myrun.data.tracking.api.RouteTrackingState
import akio.apps.myrun.data.tracking.api.model.RouteTrackingStatus
import akio.apps.myrun.feature.core.BaseViewModel
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import javax.inject.Inject
import kotlinx.coroutines.flow.map

class HomeTabViewModel @Inject constructor(
    private val routeTrackingState: RouteTrackingState,
) : BaseViewModel() {
    @Composable
    fun isTrackingStarted(): Boolean = rememberFlow(
        flow = remember {
            routeTrackingState.getTrackingStatusFlow().map { it != RouteTrackingStatus.STOPPED }
        },
        default = false
    )
}
