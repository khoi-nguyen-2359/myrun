package akio.apps.myrun.data.workout

import akio.apps.base.data.Resource
import akio.apps.myrun.data.workout.dto.WorkoutEntity
import kotlinx.coroutines.flow.Flow

interface WorkoutRepository {
    suspend fun getWorkoutsByStartTime(userId: String, startAfterTime: Long, limit: Int): List<WorkoutEntity>
}