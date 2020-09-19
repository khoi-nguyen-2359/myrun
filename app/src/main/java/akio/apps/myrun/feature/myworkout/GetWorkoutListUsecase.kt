package akio.apps.myrun.feature.myworkout

import akio.apps.myrun.feature.myworkout.model.Workout

interface GetWorkoutListUsecase {
    suspend fun getWorkoutList(startAfter: Long, perPage: Int): List<Workout>
}
