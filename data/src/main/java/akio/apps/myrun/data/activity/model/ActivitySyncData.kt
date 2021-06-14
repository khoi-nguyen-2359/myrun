package akio.apps.myrun.data.activity.model

import java.io.File

data class ActivitySyncData(
    val activityModel: ActivityModel,
    val tcxFile: File
)
