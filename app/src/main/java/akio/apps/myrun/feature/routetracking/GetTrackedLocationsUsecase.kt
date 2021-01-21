package akio.apps.myrun.feature.routetracking

import akio.apps.myrun.data.routetracking.TrackingLocationEntity

interface GetTrackedLocationsUsecase {
    suspend fun getTrackedLocations(skip: Int): List<TrackingLocationEntity>
}
