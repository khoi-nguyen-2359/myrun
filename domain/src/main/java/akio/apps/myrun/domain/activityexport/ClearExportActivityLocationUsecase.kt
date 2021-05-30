package akio.apps.myrun.domain.activityexport

import akio.apps.myrun.data.activityexport.ExportActivityLocationCache
import javax.inject.Inject

class ClearExportActivityLocationUsecase @Inject constructor(
    private val exportActivityLocationCache: ExportActivityLocationCache
) {
    suspend operator fun invoke(activityId: String) =
        exportActivityLocationCache.clearActivityLocations(activityId)
}
