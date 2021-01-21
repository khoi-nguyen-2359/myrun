package akio.apps.myrun.data.fitness._di

import akio.apps.myrun.data.fitness.FitnessDataRepository
import akio.apps.myrun.data.fitness.impl.FitnessDataRepositoryImpl
import dagger.Binds
import dagger.Module

@Module
interface FitnessDataModule {
    @Binds
    fun fitnessDataRepository(repositoryImpl: FitnessDataRepositoryImpl): FitnessDataRepository
}
