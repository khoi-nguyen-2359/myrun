package akio.apps.myrun.feature.strava

import akio.apps.myrun.feature.usertimeline.model.Activity
import java.io.File

interface ExportRunToFileUsecase {
    suspend fun exportRunToFile(activity: Activity, zip: Boolean): File
}