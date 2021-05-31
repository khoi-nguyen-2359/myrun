package akio.apps.myrun.data.activityexport.model

import java.io.File

data class TrackingRecord(
    val recordId: Int,
    val activityId: String,
    val activityName: String,
    val activityFile: File,
    val status: FileStatus,
    val target: FileTarget
)
