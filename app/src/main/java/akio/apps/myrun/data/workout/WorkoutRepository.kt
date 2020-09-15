package akio.apps.myrun.data.workout

import akio.apps.myrun.data.workout.dto.WorkoutEntity

interface WorkoutRepository {
    suspend fun getWorkoutsByStartTime(userId: String, startAfterTime: Long, limit: Int): List<WorkoutEntity>
}