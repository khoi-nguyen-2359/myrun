package akio.apps.myrun.feature.configurator._di

import akio.apps.myrun.data.tracking.wiring.DaggerTrackingDataComponent
import akio.apps.myrun.data.tracking.wiring.TrackingDataComponent
import akio.apps.myrun.domain.user.wiring.DaggerUserDomainComponent
import akio.apps.myrun.domain.user.wiring.UserDomainComponent
import akio.apps.myrun.feature.configurator.RouteTrackingConfigurationViewModel
import akio.apps.myrun.feature.configurator.viewmodel.UserAuthenticationViewModel
import dagger.Component

@Component(
    dependencies = [
        TrackingDataComponent::class,
        UserDomainComponent::class
    ]
)
interface ConfiguratorComponent {
    fun routeTrackingConfigurationViewModel(): RouteTrackingConfigurationViewModel
    fun userAuthenticationViewModel(): UserAuthenticationViewModel

    @Component.Factory
    interface Factory {
        fun create(
            trackingDataComponent: TrackingDataComponent = DaggerTrackingDataComponent.create(),
            userDomainComponent: UserDomainComponent = DaggerUserDomainComponent.factory().create()
        ): ConfiguratorComponent
    }
}
