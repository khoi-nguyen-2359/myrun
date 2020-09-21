package akio.apps.myrun.data.activity

data class RunningActivityEntity(
    val activityData: ActivityEntity,

    // info
    val routePhoto: String,

    // stats
    val averagePace: Double,
    val distance: Double,

    // data points
    val encodedPolyline: String
): ActivityEntity by activityData