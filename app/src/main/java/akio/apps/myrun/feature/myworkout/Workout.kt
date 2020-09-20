package akio.apps.myrun.feature.myworkout

import akio.apps.myrun.data.workout.ActivityType

interface Workout {
    val id: String
    val activityType: ActivityType
    val startTime: Long
    val endTime: Long
    val duration: Long
}
