package akio.apps.myrun.data.activity.api.model

import java.io.File

data class ActivityStorageData(
    val activityModel: BaseActivityModel,
    val locationDataPoints: List<ActivityLocation>,
    val routeBitmapFile: File,
)
