package akio.apps.myrun.data.workout.dto

data class FirestoreWorkout(
    val userId: String = "",
    val activityType: ActivityType = ActivityType.Unknown,

    // info
    val name: String = "",

    // stats
    val startTime: Long = 0,
    val endTime: Long = 0,

    // activity data
    val runData: FirestoreRunData? = null
)