package akio.apps.myrun.feature.strava._di

import akio.apps._base.di.AppDependantComponentFactory
import akio.apps._base.di.FeatureScope
import akio.apps._base.di.ViewModelFactoryProvider
import akio.apps.myrun._di.AppComponent
import akio.apps.myrun.data.authentication._di.AuthenticationDataModule
import akio.apps.myrun.data.externalapp._di.ExternalAppDataModule
import dagger.Component

@FeatureScope
@Component(
    modules = [
        StravaFeatureModule::class,
        ExternalAppDataModule::class,
        AuthenticationDataModule::class
    ],
    dependencies = [AppComponent::class]
)
interface StravaFeatureComponent : ViewModelFactoryProvider {
    @Component.Factory
    interface Factory : AppDependantComponentFactory<StravaFeatureComponent>
}
