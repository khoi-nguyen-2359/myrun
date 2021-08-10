package akio.apps.myrun.data.tracking.wiring

import akio.apps.base.wiring.ApplicationModule
import akio.apps.base.wiring.DispatchersModule
import akio.apps.myrun.data.tracking.api.RouteTrackingConfiguration
import akio.apps.myrun.data.tracking.api.RouteTrackingLocationRepository
import akio.apps.myrun.data.tracking.api.RouteTrackingState
import dagger.Component

@Component(
    modules = [
        TrackingDataModule::class,
        ApplicationModule::class,
        DispatchersModule::class
    ]
)
interface TrackingDataComponent {
    fun routeTrackingLocationRepo(): RouteTrackingLocationRepository
    fun routeTrackingConfiguration(): RouteTrackingConfiguration
    fun routeTrackingState(): RouteTrackingState
}
