package akio.apps.myrun.feature.configurator._di

import akio.apps.myrun.data.authentication.AuthenticationDataModule
import akio.apps.myrun.data.tracking.TrackingDataModule
import akio.apps.myrun.data.user.UserDataModule
import akio.apps.myrun.feature.configurator.RouteTrackingConfigurationViewModel
import akio.apps.myrun.feature.configurator.viewmodel.UserAuthenticationViewModel
import android.app.Application
import dagger.BindsInstance
import dagger.Component

@Component(
    modules = [
        AuthenticationDataModule::class,
        UserDataModule::class,
        TrackingDataModule::class
    ]
)
interface ConfiguratorComponent {
    fun routeTrackingConfigurationViewModel(): RouteTrackingConfigurationViewModel
    fun userAuthenticationViewModel(): UserAuthenticationViewModel

    @Component.Factory
    interface Factory {
        fun create(
            @BindsInstance application: Application,
        ): ConfiguratorComponent
    }
}
