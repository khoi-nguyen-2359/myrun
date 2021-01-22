package akio.apps.myrun.feature.strava

import akio.apps.myrun.feature.usertimeline.model.Activity
import java.io.File

interface ExportTrackingActivityToStravaFileUsecase {
    suspend fun export(activity: Activity, zip: Boolean): File
}
