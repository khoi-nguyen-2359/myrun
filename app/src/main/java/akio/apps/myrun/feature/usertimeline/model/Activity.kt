package akio.apps.myrun.feature.usertimeline.model

import akio.apps.myrun.data.activity.model.ActivityType
import akio.apps.myrun.data.recentplace.PlaceIdentifier

interface Activity {
    val id: String

    // activity info
    val activityType: ActivityType
    val name: String
    val routeImage: String
    val placeIdentifier: PlaceIdentifier?

    val startTime: Long
    val endTime: Long
    val duration: Long
    val distance: Double
    val encodedPolyline: String

    // user info
    val athleteInfo: AthleteInfo

    data class AthleteInfo(
        val userId: String,
        val userName: String?,
        val userAvatar: String?
    )
}
