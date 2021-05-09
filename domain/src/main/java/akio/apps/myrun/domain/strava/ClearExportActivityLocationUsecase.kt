package akio.apps.myrun.domain.strava

import akio.apps.myrun.data.activityfile.ExportActivityLocationRepository
import javax.inject.Inject

class ClearExportActivityLocationUsecase @Inject constructor(
    private val exportActivityLocationRepository: ExportActivityLocationRepository
) {
    suspend operator fun invoke(activityId: String) =
        exportActivityLocationRepository.clearActivityLocations(activityId)
}
