package akio.apps.myrun.data.activity.model

data class CyclingActivityModel(
    val activityData: ActivityDataModel,

    // stats
    val speed: Double,
) : ActivityModel by activityData
