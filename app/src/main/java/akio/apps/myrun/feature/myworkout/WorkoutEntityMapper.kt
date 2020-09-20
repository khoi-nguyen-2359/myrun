package akio.apps.myrun.feature.myworkout

import akio.apps.myrun.data.workout.RunningWorkoutEntity
import akio.apps.myrun.data.workout.WorkoutEntity
import javax.inject.Inject

class WorkoutEntityMapper @Inject constructor() {
    fun map(entity: WorkoutEntity): Workout {
        val workoutData = entity.run {
            WorkoutData(id, activityType, startTime, endTime, duration)
        }

        return if (entity is RunningWorkoutEntity) {
            RunningWorkout(workoutData, entity.routePhoto, entity.averagePace, entity.distance, entity.encodedPolyline)
        } else throw IllegalArgumentException("[Workout Entity] Unknown activity type")
    }
}