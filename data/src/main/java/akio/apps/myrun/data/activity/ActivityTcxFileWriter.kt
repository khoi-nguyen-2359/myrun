package akio.apps.myrun.data.activity

import akio.apps.myrun.data.activity.model.ActivityModel
import akio.apps.myrun.data.activity.model.ActivityLocation
import java.io.File

interface ActivityTcxFileWriter {
    suspend fun writeTcxFile(
        activity: ActivityModel,
        locations: List<ActivityLocation>,
        cadences: List<Int>,
        outputFile: File,
        zip: Boolean
    )
}
