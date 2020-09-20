package akio.apps.myrun.data.workout.impl

import akio.apps.myrun.data.workout.ActivityType

data class FirestoreWorkout(
    val userId: String = "",
    val activityType: ActivityType = ActivityType.Unknown,

    // info
    val name: String = "",

    // stats
    val startTime: Long = 0,
    val endTime: Long = 0,
    val duration: Long = 0,

    // activity data
    val runData: FirestoreRunData? = null
)