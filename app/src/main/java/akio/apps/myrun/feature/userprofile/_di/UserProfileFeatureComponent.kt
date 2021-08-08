package akio.apps.myrun.feature.userprofile._di

import akio.apps.base.feature.viewmodel.ViewModelFactoryProvider
import akio.apps.base.wiring.ApplicationModule
import akio.apps.base.wiring.FeatureScope
import akio.apps.myrun.data.activity.wiring.ActivityDataComponent
import akio.apps.myrun.data.activity.wiring.DaggerActivityDataComponent
import akio.apps.myrun.data.authentication.wiring.AuthenticationDataComponent
import akio.apps.myrun.data.authentication.wiring.DaggerAuthenticationDataComponent
import akio.apps.myrun.data.external.wiring.DaggerExternalAppDataComponent
import akio.apps.myrun.data.external.wiring.ExternalAppDataComponent
import akio.apps.myrun.data.user.wiring.DaggerUserDataComponent
import akio.apps.myrun.data.user.wiring.UserDataComponent
import akio.apps.myrun.feature.userprofile.UserProfileViewModel
import dagger.BindsInstance
import dagger.Component

@FeatureScope
@Component(
    modules = [UserProfileFeatureModule::class, ApplicationModule::class],
    dependencies = [
        ActivityDataComponent::class,
        UserDataComponent::class,
        AuthenticationDataComponent::class,
        ExternalAppDataComponent::class
    ]
)
interface UserProfileFeatureComponent : ViewModelFactoryProvider {
    @Component.Factory
    interface Factory {
        fun create(
            @BindsInstance params: UserProfileViewModel.Params,
            activityDataComponent: ActivityDataComponent = DaggerActivityDataComponent.create(),
            userDataComponent: UserDataComponent = DaggerUserDataComponent.create(),
            authenticationDataComponent: AuthenticationDataComponent =
                DaggerAuthenticationDataComponent.create(),
            externalAppDataComponent: ExternalAppDataComponent =
                DaggerExternalAppDataComponent.create()
        ): UserProfileFeatureComponent
    }
}
