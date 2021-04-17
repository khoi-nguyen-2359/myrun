package akio.apps.myrun.data.activity.model

data class RunningActivityModel(
    val activityData: ActivityModel,

    // stats
    val pace: Double,
    val cadence: Int
) : ActivityModel by activityData
