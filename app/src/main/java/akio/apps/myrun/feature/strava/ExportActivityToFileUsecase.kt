package akio.apps.myrun.feature.strava

import akio.apps.myrun.feature.usertimeline.model.Activity
import java.io.File

interface ExportActivityToFileUsecase {
    suspend fun exportActivityToFile(activity: Activity, zip: Boolean): File
}