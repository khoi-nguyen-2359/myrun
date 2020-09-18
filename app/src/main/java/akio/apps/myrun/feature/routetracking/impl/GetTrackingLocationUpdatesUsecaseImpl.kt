package akio.apps.myrun.feature.routetracking.impl

import akio.apps.myrun.data.routetracking.RouteTrackingLocationRepository
import akio.apps.myrun.data.routetracking.dto.TrackingLocationEntity
import akio.apps.myrun.feature.routetracking.GetTrackingLocationUpdatesUsecase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.sendBlocking
import kotlinx.coroutines.flow.*
import timber.log.Timber
import javax.inject.Inject

class GetTrackingLocationUpdatesUsecaseImpl @Inject constructor(
    private val routeTrackingLocationRepository: RouteTrackingLocationRepository
) : GetTrackingLocationUpdatesUsecase {

    override fun getLocationUpdates(drop: Int): Flow<List<TrackingLocationEntity>> = channelFlow {
        var accumDrop = drop
        while (!isClosedForSend) {
            val locationUpdates = routeTrackingLocationRepository.getRouteTrackingLocationUpdates(accumDrop)
                .filter { it.isNotEmpty() }
                .flowOn(Dispatchers.IO)
                .first()

            sendBlocking(locationUpdates)
            accumDrop += locationUpdates.size
        }

        awaitClose {
            Timber.d("getLocationUpdates closed.")
        }
    }
        .flowOn(Dispatchers.IO)
}