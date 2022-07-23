package akio.apps.myrun.data.activity.api.model

data class RunningActivityModel(
    override val activityData: ActivityDataModel,

    // stats
    val pace: Double,
    val cadence: Int,
) : BaseActivityModel(activityData)
