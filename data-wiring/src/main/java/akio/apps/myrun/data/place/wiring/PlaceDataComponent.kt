package akio.apps.myrun.data.place.wiring

import akio.apps.base.wiring.ApplicationModule
import akio.apps.base.wiring.DispatchersModule
import akio.apps.myrun.data.place.PlaceDataSource
import dagger.Component

@Component(
    modules = [
        PlaceDataModule::class,
        DispatchersModule::class,
        ApplicationModule::class
    ]
)
interface PlaceDataComponent {
    fun placeDataSource(): PlaceDataSource
}
