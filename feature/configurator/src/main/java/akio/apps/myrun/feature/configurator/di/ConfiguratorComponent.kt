package akio.apps.myrun.feature.configurator.di

import akio.apps.myrun.data.authentication.AuthenticationDataModule
import akio.apps.myrun.data.tracking.TrackingDataModule
import akio.apps.myrun.data.user.UserDataModule
import akio.apps.myrun.feature.configurator.viewmodel.RouteTrackingSectionViewModel
import akio.apps.myrun.feature.configurator.viewmodel.UserAuthenticationSectionViewModel
import android.app.Application
import dagger.BindsInstance
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(
    modules = [
        AuthenticationDataModule::class,
        UserDataModule::class,
        TrackingDataModule::class
    ]
)
interface ConfiguratorComponent {
    fun routeTrackingSectionViewModel(): RouteTrackingSectionViewModel
    fun userAuthenticationViewModel(): UserAuthenticationSectionViewModel

    @Component.Factory
    interface Factory {
        fun create(
            @BindsInstance application: Application,
        ): ConfiguratorComponent
    }
}
