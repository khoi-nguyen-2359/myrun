package akio.apps.myrun.data.workout.dto

data class FirestoreWorkout(
    val userId: String = "",
    val activityType: FirestoreActivityType = FirestoreActivityType.Unknown,

    // info
    val name: String = "",

    // stats
    val startTime: Long = 0,
    val endTime: Long = 0,

    // data points
    val heartRates: List<Float>? = null,
    val calories: List<Float>? = null,

    // activity data
    val runData: FirestoreRunData? = null
)