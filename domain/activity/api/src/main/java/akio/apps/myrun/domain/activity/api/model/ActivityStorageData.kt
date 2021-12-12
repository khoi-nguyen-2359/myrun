package akio.apps.myrun.domain.activity.api.model

import java.io.File

data class ActivityStorageData(
    val activityModel: ActivityModel,
    val locationDataPoints: List<ActivityLocation>,
    val routeBitmapFile: File
)
