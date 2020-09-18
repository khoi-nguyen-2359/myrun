package akio.apps.myrun.data.workout.dto

data class WorkoutEntityImpl(
    override val activityType: ActivityType,
    override val startTime: Long,
    override val endTime: Long
) : WorkoutEntity