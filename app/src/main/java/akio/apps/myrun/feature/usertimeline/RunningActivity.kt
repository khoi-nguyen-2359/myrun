package akio.apps.myrun.feature.usertimeline

data class RunningActivity(
    val activityData: ActivityData,

    // stats
    val pace: Double
): Activity by activityData