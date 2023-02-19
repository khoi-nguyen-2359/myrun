package akio.apps.myrun.feature.main

import akio.apps.myrun.data.tracking.api.RouteTrackingState
import akio.apps.myrun.data.tracking.api.model.RouteTrackingStatus
import akio.apps.myrun.feature.core.BaseViewModel
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class HomeTabViewModel @Inject constructor(
    private val routeTrackingState: RouteTrackingState,
) : BaseViewModel() {

    val isTrackingStartedFlow: Flow<Boolean> =
        routeTrackingState.getTrackingStatusFlow().map { it != RouteTrackingStatus.STOPPED }
}
