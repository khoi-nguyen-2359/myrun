package akio.apps.myrun.feature.usertimeline.model

import akio.apps.myrun.data.activity.ActivityType

interface Activity {
    val id: String
    val activityType: ActivityType
    val userId: String
    val name: String
    val routeImage: String
    val startTime: Long
    val endTime: Long
    val duration: Long
    val distance: Double
    val encodedPolyline: String
}
