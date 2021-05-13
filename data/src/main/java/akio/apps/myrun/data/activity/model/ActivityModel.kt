package akio.apps.myrun.data.activity.model

interface ActivityModel {
    val id: String

    // user info
    val athleteInfo: AthleteInfo

    // info
    val activityType: ActivityType
    val name: String
    val routeImage: String
    val placeName: String?

    // stats
    val startTime: Long
    val endTime: Long
    val duration: Long
    val distance: Double

    // data points
    val encodedPolyline: String

    data class AthleteInfo(
        val userId: String,
        val userName: String?,
        val userAvatar: String?
    )
}
