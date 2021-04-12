package akio.apps.myrun.feature.userprofile._di

import akio.apps._base.di.AppDependantComponentFactory
import akio.apps._base.di.FeatureScope
import akio.apps._base.di.ViewModelFactoryProvider
import akio.apps.myrun._di.AppComponent
import akio.apps.myrun.data.authentication._di.AuthenticationDataModule
import akio.apps.myrun.data.externalapp._di.ExternalAppDataModule
import akio.apps.myrun.data.userprofile._di.UserProfileDataModule
import dagger.Component

@FeatureScope
@Component(
    modules = [
        UserProfileFeatureModule::class,
        UserProfileDataModule::class,
        AuthenticationDataModule::class,
        ExternalAppDataModule::class
    ],
    dependencies = [AppComponent::class]
)
interface UserProfileFeatureComponent : ViewModelFactoryProvider {
    @Component.Factory
    interface Factory : AppDependantComponentFactory<UserProfileFeatureComponent>
}
