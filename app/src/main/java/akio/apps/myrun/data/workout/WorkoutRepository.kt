package akio.apps.myrun.data.workout

import akio.apps.myrun.data.workout.dto.WorkoutEntity
import android.graphics.Bitmap

interface WorkoutRepository {
    suspend fun getWorkoutsByStartTime(startAfterTime: Long, limit: Int): List<WorkoutEntity>
    suspend fun saveWorkout(workout: WorkoutEntity, routeMapImage: Bitmap)
}