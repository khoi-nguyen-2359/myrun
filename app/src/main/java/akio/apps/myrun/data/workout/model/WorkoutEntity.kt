package akio.apps.myrun.data.workout.model

interface WorkoutEntity {
    val id: String
    val activityType: ActivityType

    // stats
    val startTime: Long
    val endTime: Long
    val duration: Long
}