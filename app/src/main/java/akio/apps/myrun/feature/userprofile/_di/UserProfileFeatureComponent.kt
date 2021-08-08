package akio.apps.myrun.feature.userprofile._di

import akio.apps.base.wiring.FeatureScope
import akio.apps.myrun._di.AppComponent
import akio.apps.myrun.data.activity.wiring.ActivityDataComponent
import akio.apps.myrun.data.activity.wiring.DaggerActivityDataComponent
import akio.apps.myrun.data.authentication.wiring.AuthenticationDataComponent
import akio.apps.myrun.data.authentication.wiring.DaggerAuthenticationDataComponent
import akio.apps.myrun.data.external.wiring.DaggerExternalAppDataComponent
import akio.apps.myrun.data.external.wiring.ExternalAppDataComponent
import akio.apps.myrun.data.userprofile.wiring.DaggerUserProfileDataComponent
import akio.apps.myrun.data.userprofile.wiring.UserProfileDataComponent
import akio.apps.myrun.feature.userprofile.UserProfileViewModel
import android.app.Application
import dagger.BindsInstance
import dagger.Component

@FeatureScope
@Component(
    modules = [UserProfileFeatureModule::class],
    dependencies = [
        AppComponent::class,
        ActivityDataComponent::class,
        UserProfileDataComponent::class,
        AuthenticationDataComponent::class,
        ExternalAppDataComponent::class
    ]
)
interface UserProfileFeatureComponent : akio.apps.base.feature.viewmodel.ViewModelFactoryProvider {
    @Component.Factory
    interface Factory {
        fun create(
            @BindsInstance params: UserProfileViewModel.Params,
            @BindsInstance application: Application,
            appComponent: AppComponent = (application as AppComponent.Holder).getAppComponent(),
            activityDataComponent: ActivityDataComponent = DaggerActivityDataComponent.create(),
            userProfileDataComponent: UserProfileDataComponent =
                DaggerUserProfileDataComponent.create(),
            authenticationDataComponent: AuthenticationDataComponent =
                DaggerAuthenticationDataComponent.create(),
            externalAppDataComponent: ExternalAppDataComponent =
                DaggerExternalAppDataComponent.create()
        ): UserProfileFeatureComponent
    }
}
