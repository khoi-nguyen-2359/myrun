package akio.apps.myrun.data.activity.api.model

data class RunningActivityModel(
    val activityData: ActivityDataModel,

    // stats
    val pace: Double,
    val cadence: Int
) : ActivityModel by activityData