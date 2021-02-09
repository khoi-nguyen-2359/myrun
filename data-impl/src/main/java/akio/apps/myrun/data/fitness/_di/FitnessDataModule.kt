package akio.apps.myrun.data.fitness._di

import akio.apps.myrun.data.fitness.FitnessDataRepository
import akio.apps.myrun.data.fitness.impl.GoogleFitnessDataRepository
import dagger.Binds
import dagger.Module

@Module
interface FitnessDataModule {
    @Binds
    fun fitnessDataRepository(repositoryGoogle: GoogleFitnessDataRepository): FitnessDataRepository
}
