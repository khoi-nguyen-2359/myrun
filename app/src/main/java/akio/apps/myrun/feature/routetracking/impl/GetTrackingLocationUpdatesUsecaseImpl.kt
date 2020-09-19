package akio.apps.myrun.feature.routetracking.impl

import akio.apps.myrun.data.routetracking.RouteTrackingLocationRepository
import akio.apps.myrun.data.routetracking.model.TrackingLocationEntity
import akio.apps.myrun.feature.routetracking.GetTrackingLocationUpdatesUsecase
import javax.inject.Inject

class GetTrackingLocationUpdatesUsecaseImpl @Inject constructor(
    private val routeTrackingLocationRepository: RouteTrackingLocationRepository
) : GetTrackingLocationUpdatesUsecase {

    override suspend fun getLocationUpdates(skip: Int): List<TrackingLocationEntity> = routeTrackingLocationRepository.getRouteTrackingLocationUpdates(skip)
}