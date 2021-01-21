package akio.apps.myrun.feature.routetracking.usecase

import akio.apps.myrun.data.routetracking.RouteTrackingLocationRepository
import akio.apps.myrun.data.routetracking.TrackingLocationEntity
import akio.apps.myrun.feature.routetracking.GetTrackedLocationsUsecase
import javax.inject.Inject

class GetTrackedLocationsUsecaseImpl @Inject constructor(
    private val routeTrackingLocationRepository: RouteTrackingLocationRepository
) : GetTrackedLocationsUsecase {

    override suspend fun getTrackedLocations(skip: Int): List<TrackingLocationEntity> =
        routeTrackingLocationRepository.getTrackedLocations(skip)
}
