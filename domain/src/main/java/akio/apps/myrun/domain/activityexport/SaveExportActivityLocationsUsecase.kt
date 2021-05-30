package akio.apps.myrun.domain.activityexport

import akio.apps.myrun.data.activityexport.ExportActivityLocationCache
import akio.apps.myrun.data.activityexport.model.ActivityLocation
import akio.apps.myrun.data.routetracking.RouteTrackingLocationRepository
import javax.inject.Inject

class SaveExportActivityLocationsUsecase @Inject constructor(
    private val routeTrackingLocationRepository: RouteTrackingLocationRepository,
    private val exportActivityLocationCache: ExportActivityLocationCache,
) {

    suspend operator fun invoke(activityId: String) {
        val trackingLocations = routeTrackingLocationRepository.getAllLocations()
        val activityLocations = trackingLocations.map {
            ActivityLocation(activityId, it.time, it.latitude, it.longitude, it.altitude)
        }
        exportActivityLocationCache.saveActivityLocations(activityLocations)
    }
}
