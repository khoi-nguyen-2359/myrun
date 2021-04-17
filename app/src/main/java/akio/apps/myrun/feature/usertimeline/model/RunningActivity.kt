package akio.apps.myrun.feature.usertimeline.model

data class RunningActivity(
    val activityData: ActivityData,

    // stats
    val pace: Double,
    val cadence: Int
) : Activity by activityData
