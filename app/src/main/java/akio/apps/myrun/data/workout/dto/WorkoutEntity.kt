package akio.apps.myrun.data.workout.dto

interface WorkoutEntity {
    val activityType: ActivityType

    // stats
    val startTime: Long
    val endTime: Long
}