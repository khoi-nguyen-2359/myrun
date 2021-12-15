package akio.apps.myrun.data.activity.api

import akio.apps.myrun.data.activity.api.model.ActivityLocation
import akio.apps.myrun.data.activity.api.model.ActivityModel
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