package akio.apps.myrun.domain.routetracking

import akio.apps.myrun.data.activity.api.model.ActivityLocation
import akio.apps.myrun.data.tracking.api.RouteTrackingLocationRepository
import javax.inject.Inject

class GetTrackedLocationsUsecase @Inject constructor(
    private val routeTrackingLocationRepository: RouteTrackingLocationRepository,
) {

    suspend fun getTrackedLocations(skip: Int): List<ActivityLocation> =
        routeTrackingLocationRepository.getTrackedLocations(skip)
}
