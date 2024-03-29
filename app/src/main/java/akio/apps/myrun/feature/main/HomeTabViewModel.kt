package akio.apps.myrun.feature.main

import akio.apps.myrun.data.tracking.api.RouteTrackingState
import akio.apps.myrun.data.tracking.api.model.RouteTrackingStatus
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class HomeTabViewModel @Inject constructor(
    private val routeTrackingState: RouteTrackingState,
) {

    val isTrackingStartedFlow: Flow<Boolean> =
        routeTrackingState.getTrackingStatusFlow().map { it != RouteTrackingStatus.STOPPED }
}
