package akio.apps.myrun.feature.myworkout.model

import akio.apps.myrun.data.workout.model.ActivityType

interface Workout {
    val id: String
    val activityType: ActivityType
    val startTime: Long
    val endTime: Long
    val duration: Long
}
