package akio.apps.myrun.data.tracking.wiring

import akio.apps.common.wiring.ApplicationModule
import akio.apps.common.wiring.DispatchersModule
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
