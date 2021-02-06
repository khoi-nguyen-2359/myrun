package akio.apps.myrun.domain.routetracking

import akio.apps.myrun.data.routetracking.RouteTrackingLocationRepository
import akio.apps.myrun.data.routetracking.TrackingLocationEntity
import javax.inject.Inject

class GetTrackedLocationsUsecase @Inject constructor(
    private val routeTrackingLocationRepository: RouteTrackingLocationRepository
) {

    suspend fun getTrackedLocations(skip: Int): List<TrackingLocationEntity> =
        routeTrackingLocationRepository.getTrackedLocations(skip)
}
