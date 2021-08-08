package akio.apps.myrun.feature.splash._di

import akio.apps.myrun.data.authentication.wiring.AuthenticationDataComponent
import akio.apps.myrun.data.authentication.wiring.DaggerAuthenticationDataComponent
import akio.apps.myrun.feature.splash.impl.SplashViewModel
import dagger.Component

@Component(
    dependencies = [AuthenticationDataComponent::class]
)
interface SplashFeatureComponent {
    fun splashViewModel(): SplashViewModel

    @Component.Factory
    interface Factory {
        fun create(
            authenticationDataComponent: AuthenticationDataComponent =
                DaggerAuthenticationDataComponent.create()
        ): SplashFeatureComponent
    }
}
