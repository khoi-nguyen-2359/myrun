package akio.apps.myrun.data.workout.impl

import akio.apps.myrun.data.workout.WorkoutRepository
import akio.apps.myrun.feature.myworkout.model.Workout
import akio.apps.myrun.feature.myworkout.model.WorkoutEntityMapper
import androidx.paging.PagingSource
import javax.inject.Inject

class WorkoutPagingSource @Inject constructor(
    private val workoutRepository: WorkoutRepository,
    private val workoutEntityMapper: WorkoutEntityMapper
): PagingSource<Long, Workout>() {
    override suspend fun load(params: LoadParams<Long>): LoadResult<Long, Workout> {
        val startAfter = params.key ?: System.currentTimeMillis()
        val pageData = workoutRepository.getWorkoutsByStartTime(startAfter, params.loadSize)
        return LoadResult.Page(
            data = pageData.map(workoutEntityMapper::map),
            prevKey = null,
            nextKey = pageData.lastOrNull()?.startTime
        )
    }
}