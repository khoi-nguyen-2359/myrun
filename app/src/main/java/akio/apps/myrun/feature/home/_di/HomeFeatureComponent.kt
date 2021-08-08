package akio.apps.myrun.feature.home._di

import akio.apps.base.feature.viewmodel.ViewModelFactoryProvider
import akio.apps.base.wiring.DispatchersModule
import akio.apps.myrun.data.activity.wiring.ActivityDataComponent
import akio.apps.myrun.data.activity.wiring.DaggerActivityDataComponent
import akio.apps.myrun.data.authentication.wiring.AuthenticationDataComponent
import akio.apps.myrun.data.authentication.wiring.DaggerAuthenticationDataComponent
import akio.apps.myrun.data.routetracking.wiring.DaggerRouteTrackingDataComponent
import akio.apps.myrun.data.routetracking.wiring.RouteTrackingDataComponent
import akio.apps.myrun.data.user.wiring.DaggerUserDataComponent
import akio.apps.myrun.data.user.wiring.UserDataComponent
import dagger.Component

@Component(
    modules = [
        HomeFeatureModule::class,
        DispatchersModule::class,
    ],
    dependencies = [
        ActivityDataComponent::class,
        AuthenticationDataComponent::class,
        UserDataComponent::class,
        RouteTrackingDataComponent::class
    ]
)
interface HomeFeatureComponent : ViewModelFactoryProvider {
    @Component.Factory
    interface Factory {
        fun create(
            activityDataComponent: ActivityDataComponent =
                DaggerActivityDataComponent.create(),
            authenticationDataComponent: AuthenticationDataComponent =
                DaggerAuthenticationDataComponent.create(),
            userDataComponent: UserDataComponent = DaggerUserDataComponent.create(),
            routeTrackingDataComponent: RouteTrackingDataComponent =
                DaggerRouteTrackingDataComponent.create()
        ): HomeFeatureComponent
    }
}
