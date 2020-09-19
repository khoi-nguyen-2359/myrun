package akio.apps.myrun.feature.myworkout.model

import akio.apps.myrun.data.workout.model.ActivityType

data class WorkoutData(
    override val id: String,
    override val activityType: ActivityType,
    override val startTime: Long,
    override val endTime: Long,
    override val duration: Long
) : Workout