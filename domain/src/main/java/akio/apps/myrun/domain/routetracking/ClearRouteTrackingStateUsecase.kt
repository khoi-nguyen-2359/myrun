package akio.apps.myrun.domain.routetracking

import akio.apps.myrun.data.routetracking.RouteTrackingLocationRepository
import akio.apps.myrun.data.routetracking.RouteTrackingState
import javax.inject.Inject

class ClearRouteTrackingStateUsecase @Inject constructor(
    private val routeTrackingState: RouteTrackingState,
    private val routeTrackingLocationRepository: RouteTrackingLocationRepository
) {
    suspend fun clear() {
        routeTrackingState.clear()
        routeTrackingLocationRepository.clearRouteTrackingLocation()
    }
}
