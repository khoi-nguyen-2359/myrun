package akio.apps.myrun.feature.splash._di

import akio.apps.common.wiring.LaunchCatchingModule
import akio.apps.myrun.feature.splash.impl.SplashViewModel
import akio.apps.myrun.wiring.data.authentication.AuthenticationDataComponent
import akio.apps.myrun.wiring.data.authentication.DaggerAuthenticationDataComponent
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
