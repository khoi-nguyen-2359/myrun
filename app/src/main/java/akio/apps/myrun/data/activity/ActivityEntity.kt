package akio.apps.myrun.data.activity

interface ActivityEntity {
    val id: String

    // user info
    val userId: String
    val userName: String?
    val userAvatar: String?

    // info
    val activityType: ActivityType
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
