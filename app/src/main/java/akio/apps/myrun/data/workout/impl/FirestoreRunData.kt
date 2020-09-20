package akio.apps.myrun.data.workout.impl

data class FirestoreRunData(
    // info
    val routePhoto: String = "",

    // stats
    val averagePace: Double = 0.0,
    val distance: Double = 0.0,

    // data points
    val encodedPolyline: String = ""
)