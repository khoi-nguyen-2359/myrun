package akio.apps.myrun.domain.strava

import akio.apps.myrun.data.activityfile.ExportActivityLocationRepository
import akio.apps.myrun.data.activityfile.model.ActivityLocation
import akio.apps.myrun.data.routetracking.RouteTrackingLocationRepository
import javax.inject.Inject

class SaveExportActivityLocationsUsecase @Inject constructor(
    private val routeTrackingLocationRepository: RouteTrackingLocationRepository,
    private val exportActivityLocationRepository: ExportActivityLocationRepository,
) {

    suspend operator fun invoke(activityId: String) {
        val trackingLocations = routeTrackingLocationRepository.getAllLocations()
        val activityLocations = trackingLocations.map {
            ActivityLocation(activityId, it.time, it.latitude, it.longitude, it.altitude)
        }
        exportActivityLocationRepository.saveActivityLocations(activityLocations)
    }
}
