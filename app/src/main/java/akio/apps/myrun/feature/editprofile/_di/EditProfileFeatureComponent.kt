package akio.apps.myrun.feature.editprofile._di

import akio.apps._base.di.ViewModelFactoryProvider
import akio.apps.base.wiring.DispatchersModule
import akio.apps.myrun.data.authentication.wiring.AuthenticationDataComponent
import akio.apps.myrun.data.authentication.wiring.DaggerAuthenticationDataComponent
import akio.apps.myrun.data.userprofile.wiring.DaggerUserProfileDataComponent
import akio.apps.myrun.data.userprofile.wiring.UserProfileDataComponent
import dagger.Component

@Component(
    modules = [
        EditProfileFeatureModule::class,
        DispatchersModule::class
    ],
    dependencies = [UserProfileDataComponent::class, AuthenticationDataComponent::class]
)
interface EditProfileFeatureComponent : ViewModelFactoryProvider {
    @Component.Factory
    interface Factory {
        fun create(
            userProfileDataComponent: UserProfileDataComponent =
                DaggerUserProfileDataComponent.create(),
            authenticationDataComponent: AuthenticationDataComponent =
                DaggerAuthenticationDataComponent.create()
        ): EditProfileFeatureComponent
    }
}
