package akio.apps.myrun.feature.routetracking.impl

import akio.apps.myrun.data.routetracking.RouteTrackingLocationRepository
import akio.apps.myrun.data.routetracking.dto.TrackingLocationEntity
import akio.apps.myrun.feature.routetracking.GetTrackingLocationUpdatesUsecase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import javax.inject.Inject

class GetTrackingLocationUpdatesUsecaseImpl @Inject constructor(
    private val routeTrackingLocationRepository: RouteTrackingLocationRepository
) : GetTrackingLocationUpdatesUsecase {

    override suspend fun getLocationUpdates(skip: Int): List<TrackingLocationEntity> {
        return routeTrackingLocationRepository.getRouteTrackingLocationUpdates(skip)
            .filter { it.isNotEmpty() }
            .flowOn(Dispatchers.IO)
            .first()
    }
}