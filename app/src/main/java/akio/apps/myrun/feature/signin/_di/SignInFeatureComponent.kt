package akio.apps.myrun.feature.signin._di

import akio.apps._base.di.SimpleComponentFactory
import akio.apps._base.di.ViewModelFactoryProvider
import akio.apps.myrun.data.authentication._di.AuthenticationDataModule
import akio.apps.myrun.data.userprofile._di.UserProfileDataModule
import dagger.Component

@Component(
    modules = [
        SignInFeatureModule::class,
        AuthenticationDataModule::class,
        UserProfileDataModule::class
    ]
)
interface SignInFeatureComponent : ViewModelFactoryProvider {
    @Component.Factory
    interface Factory : SimpleComponentFactory<SignInFeatureComponent>
}
