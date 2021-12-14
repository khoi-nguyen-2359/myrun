package akio.apps.myrun.feature.configurator._di

import akio.apps.myrun.domain.tracking.wiring.DaggerTrackingDomainComponent
import akio.apps.myrun.domain.tracking.wiring.TrackingDomainComponent
import akio.apps.myrun.domain.user.wiring.DaggerUserDomainComponent
import akio.apps.myrun.domain.user.wiring.UserDomainComponent
import akio.apps.myrun.feature.configurator.RouteTrackingConfigurationViewModel
import akio.apps.myrun.feature.configurator.viewmodel.UserAuthenticationViewModel
import dagger.Component

@Component(
    dependencies = [
        TrackingDomainComponent::class,
        UserDomainComponent::class
    ]
)
interface ConfiguratorComponent {
    fun routeTrackingConfigurationViewModel(): RouteTrackingConfigurationViewModel
    fun userAuthenticationViewModel(): UserAuthenticationViewModel

    @Component.Factory
    interface Factory {
        fun create(
            trackingDataComponent: TrackingDomainComponent = DaggerTrackingDomainComponent.factory()
                .create(),
            userDomainComponent: UserDomainComponent = DaggerUserDomainComponent.factory().create(),
        ): ConfiguratorComponent
    }
}
