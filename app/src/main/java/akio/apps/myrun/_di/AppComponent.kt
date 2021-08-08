package akio.apps.myrun._di

import akio.apps.base.wiring.DispatchersModule
import akio.apps.myrun.MyRunApp
import akio.apps.myrun.data.authentication.wiring.AuthenticationDataComponent
import akio.apps.myrun.data.authentication.wiring.DaggerAuthenticationDataComponent
import akio.apps.myrun.data.external.wiring.DaggerExternalAppDataComponent
import akio.apps.myrun.data.external.wiring.ExternalAppDataComponent
import akio.apps.myrun.data.routetracking.wiring.DaggerRouteTrackingDataComponent
import akio.apps.myrun.data.routetracking.wiring.RouteTrackingDataComponent
import akio.apps.myrun.feature.strava._di.StravaFeatureModule
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(
    modules = [
        // modules to inject into MyRunApp
        StravaFeatureModule::class,
        DispatchersModule::class
    ],
    dependencies = [
        AuthenticationDataComponent::class,
        ExternalAppDataComponent::class,
        RouteTrackingDataComponent::class
    ]
)
interface AppComponent {
    // Application injection method
    fun inject(myRunApp: MyRunApp)

    @Component.Factory
    interface Factory {
        fun create(
            authenticationDataComponent: AuthenticationDataComponent =
                DaggerAuthenticationDataComponent.create(),
            externalAppDataComponent: ExternalAppDataComponent =
                DaggerExternalAppDataComponent.create(),
            routeTrackingDataComponent: RouteTrackingDataComponent =
                DaggerRouteTrackingDataComponent.create()
        ): AppComponent
    }

    interface Holder {
        fun getAppComponent(): AppComponent
    }
}
