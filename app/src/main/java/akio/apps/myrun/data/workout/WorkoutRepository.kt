package akio.apps.myrun.data.workout

import android.graphics.Bitmap

interface WorkoutRepository {
    suspend fun getWorkoutsByStartTime(startAfterTime: Long, limit: Int): List<WorkoutEntity>
    suspend fun saveWorkout(workout: WorkoutEntity, routeMapImage: Bitmap)
}