package akio.apps.myrun.wiring.data.fitness

import akio.apps.myrun.data.fitness.FitnessDataRepository
import akio.apps.myrun.data.fitness.impl.GoogleFitnessDataRepository
import dagger.Binds
import dagger.Module

@Module
internal interface FitnessDataModule {
    @Binds
    fun fitnessDataRepository(repositoryGoogle: GoogleFitnessDataRepository): FitnessDataRepository
}
