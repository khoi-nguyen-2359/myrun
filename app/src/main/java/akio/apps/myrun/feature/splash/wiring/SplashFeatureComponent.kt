package akio.apps.myrun.feature.splash.wiring

import akio.apps.myrun.data.authentication.wiring.AuthenticationDataComponent
import akio.apps.myrun.data.authentication.wiring.DaggerAuthenticationDataComponent
import akio.apps.myrun.data.wiring.LaunchCatchingModule
import akio.apps.myrun.feature.splash.impl.SplashViewModel
import dagger.Component

@Component(
    modules = [LaunchCatchingModule::class],
    dependencies = [AuthenticationDataComponent::class]
)
interface SplashFeatureComponent {
    fun splashViewModel(): SplashViewModel

    @Component.Factory
    interface Factory {
        fun create(
            authenticationDataComponent: AuthenticationDataComponent =
                DaggerAuthenticationDataComponent.create(),
        ): SplashFeatureComponent
    }
}
