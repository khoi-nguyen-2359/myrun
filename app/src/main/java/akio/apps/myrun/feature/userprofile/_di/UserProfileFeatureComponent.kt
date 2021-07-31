package akio.apps.myrun.feature.userprofile._di

import akio.apps._base.di.FeatureScope
import akio.apps._base.di.ViewModelFactoryProvider
import akio.apps.myrun._di.AppComponent
import akio.apps.myrun._di.DispatchersModule
import akio.apps.myrun.data.activity._di.ActivityDataModule
import akio.apps.myrun.data.authentication._di.AuthenticationDataModule
import akio.apps.myrun.data.externalapp._di.ExternalAppDataModule
import akio.apps.myrun.data.userprofile._di.UserProfileDataModule
import akio.apps.myrun.feature.userprofile.UserProfileViewModel
import android.app.Application
import dagger.BindsInstance
import dagger.Component

@FeatureScope
@Component(
    modules = [
        UserProfileFeatureModule::class,
        UserProfileDataModule::class,
        AuthenticationDataModule::class,
        ExternalAppDataModule::class,
        DispatchersModule::class,
        ActivityDataModule::class
    ],
    dependencies = [AppComponent::class]
)
interface UserProfileFeatureComponent : ViewModelFactoryProvider {
    @Component.Factory
    interface Factory {
        fun create(
            @BindsInstance params: UserProfileViewModel.Params,
            @BindsInstance application: Application,
            appComponent: AppComponent = (application as AppComponent.Holder).getAppComponent()
        ): UserProfileFeatureComponent
    }
}
