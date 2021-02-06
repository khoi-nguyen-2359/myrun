package akio.apps.myrun.feature.usertimeline.model

data class CyclingActivity(
    val activityData: ActivityData,

    // stats
    val speed: Double
) : Activity by activityData
