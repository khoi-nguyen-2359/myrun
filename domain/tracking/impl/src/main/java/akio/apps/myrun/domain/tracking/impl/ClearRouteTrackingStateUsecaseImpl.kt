package akio.apps.myrun.domain.tracking.impl

import akio.apps.myrun.domain.tracking.api.ClearRouteTrackingStateUsecase
import akio.apps.myrun.domain.tracking.api.RouteTrackingLocationRepository
import akio.apps.myrun.domain.tracking.api.RouteTrackingState
import javax.inject.Inject

class ClearRouteTrackingStateUsecaseImpl @Inject constructor(
    private val routeTrackingState: RouteTrackingState,
    private val routeTrackingLocationRepository: RouteTrackingLocationRepository
) : ClearRouteTrackingStateUsecase {
    override suspend fun clear() {
        routeTrackingState.clear()
        routeTrackingLocationRepository.clearRouteTrackingLocation()
    }
}
