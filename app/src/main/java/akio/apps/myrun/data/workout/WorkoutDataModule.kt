package akio.apps.myrun.data.workout

import akio.apps.myrun.data.workout.impl.WorkoutRepositoryImpl
import dagger.Binds
import dagger.Module

@Module
interface WorkoutDataModule {
    @Binds
    fun workoutRepo(repositoryImpl: WorkoutRepositoryImpl): WorkoutRepository
}