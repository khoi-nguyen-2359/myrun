package akio.apps.myrun.feature.configuration.impl._di

import akio.apps.myrun.data.tracking.wiring.DaggerTrackingDataComponent
import akio.apps.myrun.data.tracking.wiring.TrackingDataComponent
import akio.apps.myrun.feature.configuration.impl.RouteTrackingConfigurationViewModel
import dagger.Component

@Component(dependencies = [TrackingDataComponent::class])
interface ConfiguratorComponent {
    fun routeTrackingConfigurationViewModel(): RouteTrackingConfigurationViewModel

    @Component.Factory
    interface Factory {
        fun create(
            trackingDataComponent: TrackingDataComponent = DaggerTrackingDataComponent.create()
        ): ConfiguratorComponent
    }
}
