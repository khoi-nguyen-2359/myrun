package akio.apps.myrun.feature.editprofile._di

import akio.apps.common.feature.viewmodel.ViewModelFactoryProvider
import akio.apps.common.wiring.DispatchersModule
import akio.apps.myrun.data.authentication.wiring.AuthenticationDataComponent
import akio.apps.myrun.data.authentication.wiring.DaggerAuthenticationDataComponent
import akio.apps.myrun.data.user.wiring.DaggerUserDataComponent
import akio.apps.myrun.data.user.wiring.UserDataComponent
import dagger.Component

@Component(
    modules = [EditProfileFeatureModule::class, DispatchersModule::class],
    dependencies = [akio.apps.myrun.data.user.wiring.UserDataComponent::class, AuthenticationDataComponent::class]
)
interface EditProfileFeatureComponent : ViewModelFactoryProvider {
    @Component.Factory
    interface Factory {
        fun create(
            userDataComponent: akio.apps.myrun.data.user.wiring.UserDataComponent = DaggerUserDataComponent.create(),
            authenticationDataComponent: AuthenticationDataComponent =
                DaggerAuthenticationDataComponent.create()
        ): EditProfileFeatureComponent
    }
}
