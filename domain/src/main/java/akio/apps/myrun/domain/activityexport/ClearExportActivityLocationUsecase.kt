package akio.apps.myrun.domain.activityexport

import akio.apps.myrun.data.activitysharing.ActivityLocationCache
import javax.inject.Inject

class ClearExportActivityLocationUsecase @Inject constructor(
    private val activityLocationCache: ActivityLocationCache
) {
    suspend operator fun invoke(activityId: String) =
        activityLocationCache.clearActivityLocations(activityId)
}
