package akio.apps.myrun.feature.configurator.di

import akio.apps.myrun.base.di.DispatchersModule
import akio.apps.myrun.base.di.FeatureScope
import akio.apps.myrun.data.authentication.di.AuthenticationDataComponent
import akio.apps.myrun.data.authentication.di.DaggerAuthenticationDataComponent
import akio.apps.myrun.data.tracking.di.DaggerTrackingDataComponent
import akio.apps.myrun.data.tracking.di.TrackingDataComponent
import akio.apps.myrun.feature.configurator.viewmodel.LocationPresentViewModel
import akio.apps.myrun.feature.configurator.viewmodel.RouteTrackingSectionViewModel
import akio.apps.myrun.feature.configurator.viewmodel.UserAuthenticationSectionViewModel
import android.app.Application
import dagger.BindsInstance
import dagger.Component

@FeatureScope
@Component(
    modules = [DispatchersModule::class],
    dependencies = [
        AuthenticationDataComponent::class,
        TrackingDataComponent::class
    ]
)
interface ConfiguratorComponent {
    fun routeTrackingSectionViewModel(): RouteTrackingSectionViewModel
    fun locationPresentViewModel(): LocationPresentViewModel
    fun userAuthenticationViewModel(): UserAuthenticationSectionViewModel

    @Component.Factory
    interface Factory {
        fun create(
            @BindsInstance application: Application,
            authenticationDataComponent: AuthenticationDataComponent =
                DaggerAuthenticationDataComponent.factory().create(application),
            trackingDataComponent: TrackingDataComponent =
                DaggerTrackingDataComponent.factory().create(application),
        ): ConfiguratorComponent
    }
}
