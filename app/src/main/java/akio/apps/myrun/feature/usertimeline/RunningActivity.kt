package akio.apps.myrun.feature.usertimeline

data class RunningActivity(
    val activityData: ActivityData,

    // info
    val routePhoto: String,

    // stats
    val averagePace: Double,
    val distance: Double,

    // data points
    val encodedPolyline: String
): Activity by activityData