package akio.apps.myrun.wiring.data.tracking

import akio.apps.myrun.data.tracking.api.RouteTrackingConfiguration
import akio.apps.myrun.data.tracking.api.RouteTrackingLocationRepository
import akio.apps.myrun.data.tracking.api.RouteTrackingState
import akio.apps.myrun.data.wiring.ApplicationModule
import akio.apps.myrun.data.wiring.DispatchersModule
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
