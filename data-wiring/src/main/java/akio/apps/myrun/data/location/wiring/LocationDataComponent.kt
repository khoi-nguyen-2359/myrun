package akio.apps.myrun.data.location.wiring

import akio.apps.base.wiring.ApplicationModule
import akio.apps.myrun.data.location.LocationDataSource
import dagger.Component

@Component(
    modules = [
        LocationDataModule::class,
        ApplicationModule::class
    ]
)
interface LocationDataComponent {
    fun locationDataSource(): LocationDataSource
}
