package akio.apps.myrun.feature.routetracking

import akio.apps.myrun.data.routetracking.dto.TrackingLocationEntity
import kotlinx.coroutines.flow.Flow

interface GetTrackingLocationUpdatesUsecase {
    suspend fun getLocationUpdates(skip: Int): List<TrackingLocationEntity>
}