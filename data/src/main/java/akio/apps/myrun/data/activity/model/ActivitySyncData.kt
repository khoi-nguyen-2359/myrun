package akio.apps.myrun.data.activity.model

import akio.apps.myrun.data.activity.model.ActivityModel
import java.io.File

data class ActivitySyncData(
    val activityModel: ActivityModel,
    val tcxFile: File
)
