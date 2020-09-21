package akio.apps.myrun.feature.usertimeline

import akio.apps.myrun.data.activity.ActivityType

interface Activity {
    val id: String
    val activityType: ActivityType
    val startTime: Long
    val endTime: Long
    val duration: Long
}
