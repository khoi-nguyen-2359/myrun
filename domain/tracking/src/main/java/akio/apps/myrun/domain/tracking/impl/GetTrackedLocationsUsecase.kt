package akio.apps.myrun.domain.tracking.impl

import akio.apps.myrun.data.tracking.api.RouteTrackingLocationRepository
import akio.apps.myrun.domain.activity.api.model.ActivityLocation
import javax.inject.Inject

class GetTrackedLocationsUsecase @Inject constructor(
    private val routeTrackingLocationRepository: RouteTrackingLocationRepository,
) {

    suspend fun getTrackedLocations(skip: Int): List<ActivityLocation> =
        routeTrackingLocationRepository.getTrackedLocations(skip)
}
