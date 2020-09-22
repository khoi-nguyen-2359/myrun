package akio.apps.myrun.feature.usertimeline.model

data class RunningActivity(
    val activityData: ActivityData,

    // stats
    val pace: Double
): Activity by activityData