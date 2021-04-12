package akio.apps.myrun.feature.splash._di

import akio.apps._base.di.SimpleComponentFactory
import akio.apps._base.di.ViewModelFactoryProvider
import akio.apps.myrun.data.authentication._di.AuthenticationDataModule
import dagger.Component

@Component(
    modules = [
        SplashFeatureModule::class,
        AuthenticationDataModule::class
    ]
)
interface SplashFeatureComponent : ViewModelFactoryProvider {
    @Component.Factory
    interface Factory : SimpleComponentFactory<SplashFeatureComponent>
}
