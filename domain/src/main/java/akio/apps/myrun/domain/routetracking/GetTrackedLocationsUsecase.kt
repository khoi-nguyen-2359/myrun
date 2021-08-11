package akio.apps.myrun.domain.routetracking

import akio.apps.myrun.data.location.api.model.Location
import akio.apps.myrun.data.tracking.api.RouteTrackingLocationRepository
import javax.inject.Inject

class GetTrackedLocationsUsecase @Inject constructor(
    private val routeTrackingLocationRepository: RouteTrackingLocationRepository
) {

    suspend fun getTrackedLocations(skip: Int): List<Location> =
        routeTrackingLocationRepository.getTrackedLocations(skip)
}
