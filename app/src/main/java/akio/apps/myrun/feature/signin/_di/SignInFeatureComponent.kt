package akio.apps.myrun.feature.signin._di

import akio.apps._base.di.AppDependantComponentFactory
import akio.apps._base.di.FeatureScope
import akio.apps._base.di.ViewModelFactoryProvider
import akio.apps.myrun._di.AppComponent
import akio.apps.myrun._di.DispatchersModule
import akio.apps.myrun.data.authentication._di.AuthenticationDataModule
import akio.apps.myrun.data.externalapp._di.ExternalAppDataModule
import akio.apps.myrun.data.userprofile._di.UserProfileDataModule
import dagger.Component

@FeatureScope
@Component(
    modules = [
        SignInFeatureModule::class,
        AuthenticationDataModule::class,
        UserProfileDataModule::class,
        DispatchersModule::class,
        ExternalAppDataModule::class
    ],
    dependencies = [AppComponent::class]
)
interface SignInFeatureComponent : ViewModelFactoryProvider {
    @Component.Factory
    interface Factory : AppDependantComponentFactory<SignInFeatureComponent>
}
