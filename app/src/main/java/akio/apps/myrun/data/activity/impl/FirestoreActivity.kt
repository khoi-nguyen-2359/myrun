package akio.apps.myrun.data.activity.impl

import akio.apps.myrun.data.activity.ActivityType

data class FirestoreActivity(
    val userId: String = "",
    val activityType: ActivityType = ActivityType.Unknown,

    // info
    val name: String = "",
    val routeImage: String = "",

    // stats
    val startTime: Long = 0,
    val endTime: Long = 0,
    val duration: Long = 0,
    val distance: Double = 0.0,
    val encodedPolyline: String = "",

    // activity data
    val runningData: FirestoreRunningData? = null,
    val cyclingData: FirestoreCyclingData? = null
)