package akio.apps.myrun.feature.configuration.impl._di

import akio.apps.myrun.data.routetracking.wiring.DaggerRouteTrackingDataComponent
import akio.apps.myrun.data.routetracking.wiring.RouteTrackingDataComponent
import akio.apps.myrun.feature.configuration.impl.RouteTrackingConfigurationViewModel
import dagger.Component

@Component(dependencies = [RouteTrackingDataComponent::class])
interface ConfiguratorComponent {
    fun routeTrackingConfigurationViewModel(): RouteTrackingConfigurationViewModel

    @Component.Factory
    interface Factory {
        fun create(
            routeTrackingDataComponent: RouteTrackingDataComponent =
                DaggerRouteTrackingDataComponent.create()
        ): ConfiguratorComponent
    }
}
