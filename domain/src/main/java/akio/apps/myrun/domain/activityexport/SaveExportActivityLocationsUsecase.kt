package akio.apps.myrun.domain.activityexport

import akio.apps.myrun.data.activitysharing.ActivityLocationCache
import akio.apps.myrun.data.activitysharing.model.ActivityLocation
import akio.apps.myrun.data.routetracking.RouteTrackingLocationRepository
import javax.inject.Inject

class SaveExportActivityLocationsUsecase @Inject constructor(
    private val routeTrackingLocationRepository: RouteTrackingLocationRepository,
    private val activityLocationCache: ActivityLocationCache,
) {

    suspend operator fun invoke(activityId: String) {
        val trackingLocations = routeTrackingLocationRepository.getAllLocations()
        val activityLocations = trackingLocations.map {
            ActivityLocation(activityId, it.time, it.latitude, it.longitude, it.altitude)
        }
        activityLocationCache.saveActivityLocations(activityLocations)
    }
}
