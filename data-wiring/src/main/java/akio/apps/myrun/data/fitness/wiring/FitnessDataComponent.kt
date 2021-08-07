package akio.apps.myrun.data.fitness.wiring

import akio.apps.base.wiring.ApplicationModule
import akio.apps.base.wiring.DispatchersModule
import akio.apps.myrun.data._base.wiring.FirebaseDataModule
import akio.apps.myrun.data.fitness.FitnessDataRepository
import dagger.Component

@Component(modules = [
    FitnessDataModule::class,
    DispatchersModule::class,
    FirebaseDataModule::class,
    ApplicationModule::class
])
interface FitnessDataComponent {
    fun fitnessDataRepository(): FitnessDataRepository
}
