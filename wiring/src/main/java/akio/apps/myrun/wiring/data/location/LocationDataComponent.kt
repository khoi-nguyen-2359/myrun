package akio.apps.myrun.wiring.data.location

import akio.apps.common.wiring.ApplicationModule
import akio.apps.common.wiring.DispatchersModule
import akio.apps.myrun.data.location.api.LocationDataSource
import akio.apps.myrun.data.location.api.PlaceDataSource
import dagger.Component

@Component(
    modules = [
        LocationDataModule::class,
        ApplicationModule::class,
        DispatchersModule::class
    ]
)
interface LocationDataComponent {
    fun placeDataSource(): PlaceDataSource
    fun locationDataSource(): LocationDataSource
}
