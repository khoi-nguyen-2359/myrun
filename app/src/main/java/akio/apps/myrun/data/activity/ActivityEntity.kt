package akio.apps.myrun.data.activity

interface ActivityEntity {
    val id: String
    val userId: String
    val activityType: ActivityType

    // info
    val name: String
    val routeImage: String

    // stats
    val startTime: Long
    val endTime: Long
    val duration: Long
    val distance: Double

    // data points
    val encodedPolyline: String
}