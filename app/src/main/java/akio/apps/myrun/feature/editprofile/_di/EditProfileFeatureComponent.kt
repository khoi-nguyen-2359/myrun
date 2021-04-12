package akio.apps.myrun.feature.editprofile._di

import akio.apps._base.di.SimpleComponentFactory
import akio.apps._base.di.ViewModelFactoryProvider
import akio.apps.myrun.data.authentication._di.AuthenticationDataModule
import akio.apps.myrun.data.userprofile._di.UserProfileDataModule
import dagger.Component

@Component(
    modules = [
        EditProfileFeatureModule::class,
        UserProfileDataModule::class,
        AuthenticationDataModule::class
    ]
)
interface EditProfileFeatureComponent : ViewModelFactoryProvider {
    @Component.Factory
    interface Factory : SimpleComponentFactory<EditProfileFeatureComponent>
}
