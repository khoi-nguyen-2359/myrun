package akio.apps.myrun.data.activity.api.model

interface ActivityModel {
    val id: String

    // user info
    val athleteInfo: AthleteInfo

    // info
    val activityType: ActivityType
    val name: String
    val routeImage: String
    val placeIdentifier: String?

    // stats
    val startTime: Long
    val endTime: Long
    val duration: Long
    val distance: Double

    // data points
    val encodedPolyline: String

    data class AthleteInfo(
        val userId: String = "",
        val userName: String? = null,
        val userAvatar: String? = null
    )
}
