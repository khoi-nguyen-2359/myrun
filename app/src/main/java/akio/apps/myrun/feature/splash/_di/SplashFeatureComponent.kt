package akio.apps.myrun.feature.splash._di

import akio.apps._base.di.ViewModelFactoryProvider
import akio.apps.myrun.data.authentication.wiring.AuthenticationDataComponent
import akio.apps.myrun.data.authentication.wiring.DaggerAuthenticationDataComponent
import dagger.Component

@Component(
    modules = [
        SplashFeatureModule::class
    ],
    dependencies = [AuthenticationDataComponent::class]
)
interface SplashFeatureComponent : ViewModelFactoryProvider {
    @Component.Factory
    interface Factory {
        fun create(
            authenticationDataComponent: AuthenticationDataComponent =
                DaggerAuthenticationDataComponent.create()
        ): SplashFeatureComponent
    }
}
