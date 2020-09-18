package akio.apps.myrun.feature.routetracking.impl

import akio.apps.myrun.data.routetracking.RouteTrackingLocationRepository
import akio.apps.myrun.data.routetracking.RouteTrackingState
import akio.apps.myrun.feature.routetracking.ClearRouteTrackingStateUsecase
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