package akio.apps.myrun.domain.tracking.impl

import akio.apps.myrun.data.tracking.api.RouteTrackingLocationRepository
import akio.apps.myrun.data.tracking.api.RouteTrackingState
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
