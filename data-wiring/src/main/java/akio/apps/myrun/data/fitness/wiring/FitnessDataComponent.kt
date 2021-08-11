package akio.apps.myrun.data.fitness.wiring

import akio.apps.common.wiring.ApplicationModule
import akio.apps.common.wiring.DispatchersModule
import akio.apps.myrun.data.wiring.FirebaseDataModule
import akio.apps.myrun.data.fitness.FitnessDataRepository
import dagger.Component

@Component(
    modules = [
        FitnessDataModule::class,
        DispatchersModule::class,
        akio.apps.myrun.data.wiring.FirebaseDataModule::class,
        ApplicationModule::class
    ]
)
interface FitnessDataComponent {
    fun fitnessDataRepository(): FitnessDataRepository
}
