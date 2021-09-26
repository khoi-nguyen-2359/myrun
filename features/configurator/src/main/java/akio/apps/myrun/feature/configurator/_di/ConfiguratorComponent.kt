package akio.apps.myrun.feature.configurator._di

import akio.apps.myrun.feature.configurator.RouteTrackingConfigurationViewModel
import akio.apps.myrun.feature.configurator.viewmodel.UserAuthenticationViewModel
import akio.apps.myrun.wiring.data.tracking.DaggerTrackingDataComponent
import akio.apps.myrun.wiring.data.tracking.TrackingDataComponent
import akio.apps.myrun.wiring.domain.DaggerDomainComponent
import akio.apps.myrun.wiring.domain.DomainComponent
import dagger.Component

@Component(
    dependencies = [
        TrackingDataComponent::class,
        DomainComponent::class
    ]
)
interface ConfiguratorComponent {
    fun routeTrackingConfigurationViewModel(): RouteTrackingConfigurationViewModel
    fun userAuthenticationViewModel(): UserAuthenticationViewModel

    @Component.Factory
    interface Factory {
        fun create(
            trackingDataComponent: TrackingDataComponent = DaggerTrackingDataComponent.create(),
            domainComponent: DomainComponent = DaggerDomainComponent.factory().create(),
        ): ConfiguratorComponent
    }
}
