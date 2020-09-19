package akio.apps.myrun.feature.routetracking

import akio.apps.myrun.data.routetracking.model.TrackingLocationEntity

interface GetTrackingLocationUpdatesUsecase {
    suspend fun getLocationUpdates(skip: Int): List<TrackingLocationEntity>
}