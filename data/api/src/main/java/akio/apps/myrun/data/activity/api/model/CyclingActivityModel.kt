package akio.apps.myrun.data.activity.api.model

data class CyclingActivityModel(
    override val activityData: ActivityDataModel,

    // stats
    val speed: Double,
) : BaseActivityModel(activityData)
