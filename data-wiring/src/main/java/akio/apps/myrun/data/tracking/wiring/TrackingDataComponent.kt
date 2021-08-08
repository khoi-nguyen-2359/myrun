package akio.apps.myrun.data.tracking.wiring

import akio.apps.base.wiring.ApplicationModule
import akio.apps.base.wiring.DispatchersModule
import akio.apps.myrun.data.location.LocationDataSource
import akio.apps.myrun.data.place.PlaceDataSource
import akio.apps.myrun.data.routetracking.RouteTrackingConfiguration
import akio.apps.myrun.data.routetracking.RouteTrackingLocationRepository
import akio.apps.myrun.data.routetracking.RouteTrackingState
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
    fun placeDataSource(): PlaceDataSource
    fun locationDataSource(): LocationDataSource
}
