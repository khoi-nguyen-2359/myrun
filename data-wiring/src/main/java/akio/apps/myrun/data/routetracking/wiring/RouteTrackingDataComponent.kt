package akio.apps.myrun.data.routetracking.wiring

import akio.apps.base.wiring.ApplicationModule
import akio.apps.base.wiring.DispatchersModule
import akio.apps.myrun.data.routetracking.RouteTrackingConfiguration
import akio.apps.myrun.data.routetracking.RouteTrackingLocationRepository
import akio.apps.myrun.data.routetracking.RouteTrackingState
import dagger.Component

@Component(
    modules = [
        RouteTrackingDataModule::class,
        ApplicationModule::class,
        DispatchersModule::class
    ]
)
interface RouteTrackingDataComponent {
    fun routeTrackingLocationRepo(): RouteTrackingLocationRepository
    fun routeTrackingConfiguration(): RouteTrackingConfiguration
    fun routeTrackingState(): RouteTrackingState
}
