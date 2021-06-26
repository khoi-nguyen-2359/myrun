package akio.apps.myrun.domain.routetracking

import akio.apps.myrun.data.location.LocationEntity
import akio.apps.myrun.data.routetracking.RouteTrackingLocationRepository
import javax.inject.Inject

class GetTrackedLocationsUsecase @Inject constructor(
    private val routeTrackingLocationRepository: RouteTrackingLocationRepository
) {

    suspend fun getTrackedLocations(skip: Int): List<LocationEntity> =
        routeTrackingLocationRepository.getTrackedLocations(skip)
}
