package akio.apps.myrun.data.activity.model

data class CyclingActivityModel(
    val activityData: ActivityModel,

    // stats
    val speed: Double,
) : ActivityModel by activityData
