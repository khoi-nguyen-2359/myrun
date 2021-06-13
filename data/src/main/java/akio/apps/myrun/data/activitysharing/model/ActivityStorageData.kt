package akio.apps.myrun.data.activitysharing.model

import akio.apps.myrun.data.activity.model.ActivityModel
import java.io.File

data class ActivityStorageData(
    val activityModel: ActivityModel,
    val locationDataPoints: List<ActivityLocation>,
    val routeBitmapFile: File
)
