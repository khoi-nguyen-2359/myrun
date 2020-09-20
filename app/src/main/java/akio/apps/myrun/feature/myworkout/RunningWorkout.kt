package akio.apps.myrun.feature.myworkout

data class RunningWorkout(
    val workoutData: WorkoutData,

    // info
    val routePhoto: String,

    // stats
    val averagePace: Double,
    val distance: Double,

    // data points
    val encodedPolyline: String
): Workout by workoutData