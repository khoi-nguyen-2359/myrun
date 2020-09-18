package akio.apps.myrun.feature.myworkout.impl

import akio.apps.myrun.data.workout.WorkoutRepository
import akio.apps.myrun.data.workout.dto.WorkoutEntity
import akio.apps.myrun.feature.myworkout.GetWorkoutListUsecase
import akio.apps.myrun.feature.myworkout.model.Workout
import akio.apps.myrun.feature.myworkout.model.WorkoutEntityMapper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class GetWorkoutListUsecaseImpl @Inject constructor(
    private val workoutRepository: WorkoutRepository,
    private val workoutEntityMapper: WorkoutEntityMapper
) : GetWorkoutListUsecase {

    override suspend fun getWorkoutList(startAfter: Long, perPage: Int): List<Workout> = withContext(Dispatchers.IO) {
        workoutRepository.getWorkoutsByStartTime(startAfter, perPage).map(workoutEntityMapper::map)
    }
}