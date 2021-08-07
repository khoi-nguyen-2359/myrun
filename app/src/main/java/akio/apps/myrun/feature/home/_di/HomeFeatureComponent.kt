package akio.apps.myrun.feature.home._di

import akio.apps._base.di.ViewModelFactoryProvider
import akio.apps.base.wiring.DispatchersModule
import akio.apps.myrun.data.activity.wiring.ActivityDataComponent
import akio.apps.myrun.data.activity.wiring.DaggerActivityDataComponent
import akio.apps.myrun.data.authentication.wiring.AuthenticationDataComponent
import akio.apps.myrun.data.authentication.wiring.DaggerAuthenticationDataComponent
import akio.apps.myrun.data.recentplace.wiring.DaggerRecentPlaceDataComponent
import akio.apps.myrun.data.recentplace.wiring.RecentPlaceDataComponent
import akio.apps.myrun.data.routetracking.wiring.DaggerRouteTrackingDataComponent
import akio.apps.myrun.data.routetracking.wiring.RouteTrackingDataComponent
import akio.apps.myrun.data.userfollow.wiring.DaggerUserFollowDataComponent
import akio.apps.myrun.data.userfollow.wiring.UserFollowDataComponent
import android.app.Application
import dagger.BindsInstance
import dagger.Component

@Component(
    modules = [
        HomeFeatureModule::class,
        DispatchersModule::class,
    ],
    dependencies = [
        ActivityDataComponent::class,
        AuthenticationDataComponent::class,
        UserFollowDataComponent::class,
        RecentPlaceDataComponent::class,
        RouteTrackingDataComponent::class
    ]
)
interface HomeFeatureComponent : ViewModelFactoryProvider {
    @Component.Factory
    interface Factory {
        fun create(
            @BindsInstance application: Application,
            activityDataComponent: ActivityDataComponent =
                DaggerActivityDataComponent.create(),
            authenticationDataComponent: AuthenticationDataComponent =
                DaggerAuthenticationDataComponent.create(),
            userFollowDataComponent: UserFollowDataComponent =
                DaggerUserFollowDataComponent.create(),
            recentPlaceDataComponent: RecentPlaceDataComponent =
                DaggerRecentPlaceDataComponent.create(),
            routeTrackingDataComponent: RouteTrackingDataComponent =
                DaggerRouteTrackingDataComponent.create()
        ): HomeFeatureComponent
    }
}
