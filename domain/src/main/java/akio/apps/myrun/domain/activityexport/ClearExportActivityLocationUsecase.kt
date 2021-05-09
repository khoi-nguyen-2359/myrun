package akio.apps.myrun.domain.activityexport

import akio.apps.myrun.data.activityexport.ExportActivityLocationRepository
import javax.inject.Inject

class ClearExportActivityLocationUsecase @Inject constructor(
    private val exportActivityLocationRepository: ExportActivityLocationRepository
) {
    suspend operator fun invoke(activityId: String) =
        exportActivityLocationRepository.clearActivityLocations(activityId)
}
