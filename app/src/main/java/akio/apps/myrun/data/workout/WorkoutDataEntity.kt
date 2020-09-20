package akio.apps.myrun.data.workout

data class WorkoutDataEntity(
    override val id: String,
    override val activityType: ActivityType,
    override val startTime: Long,
    override val endTime: Long,
    override val duration: Long
) : WorkoutEntity