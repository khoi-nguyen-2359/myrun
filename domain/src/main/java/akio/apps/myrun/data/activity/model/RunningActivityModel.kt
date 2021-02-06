package akio.apps.myrun.data.activity.model

data class RunningActivityModel(
    val activityData: ActivityModel,

    // stats
    val pace: Double,
) : ActivityModel by activityData
