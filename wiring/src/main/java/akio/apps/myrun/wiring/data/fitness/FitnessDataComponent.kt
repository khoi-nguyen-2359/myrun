package akio.apps.myrun.wiring.data.fitness

import akio.apps.common.wiring.ApplicationModule
import akio.apps.common.wiring.DispatchersModule
import akio.apps.myrun.data.fitness.FitnessDataRepository
import akio.apps.myrun.data.wiring.FirebaseDataModule
import dagger.Component

@Component(
    modules = [
        FitnessDataModule::class,
        DispatchersModule::class,
        FirebaseDataModule::class,
        ApplicationModule::class
    ]
)
interface FitnessDataComponent {
    fun fitnessDataRepository(): FitnessDataRepository
}
