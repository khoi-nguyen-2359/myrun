package akio.apps.myrun.data.fitness.wiring

import akio.apps.myrun.data.fitness.FitnessDataRepository
import akio.apps.myrun.data.fitness.impl.GoogleFitnessDataRepository
import dagger.Binds
import dagger.Module

@Module
internal interface FitnessDataModule {
    @Binds
    fun fitnessDataRepository(repositoryGoogle: GoogleFitnessDataRepository): FitnessDataRepository
}
