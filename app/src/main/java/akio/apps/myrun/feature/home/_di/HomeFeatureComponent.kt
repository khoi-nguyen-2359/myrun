package akio.apps.myrun.feature.home._di

import akio.apps._base.di.ViewModelFactoryProvider
import akio.apps.myrun._di.DispatchersModule
import akio.apps.myrun.data.activity._di.ActivityDataModule
import akio.apps.myrun.data.authentication._di.AuthenticationDataModule
import akio.apps.myrun.data.recentplace._di.RecentPlaceDataModule
import akio.apps.myrun.data.routetracking._di.RouteTrackingDataModule
import akio.apps.myrun.data.userfollow._di.UserFollowDataModule
import android.app.Application
import dagger.BindsInstance
import dagger.Component

@Component(
    modules = [
        HomeFeatureModule::class,
        ActivityDataModule::class,
        AuthenticationDataModule::class,
        UserFollowDataModule::class,
        DispatchersModule::class,
        RecentPlaceDataModule::class,
        RouteTrackingDataModule::class
    ]
)
interface HomeFeatureComponent : ViewModelFactoryProvider {
    @Component.Factory
    interface Factory {
        fun create(@BindsInstance application: Application): HomeFeatureComponent
    }
}
