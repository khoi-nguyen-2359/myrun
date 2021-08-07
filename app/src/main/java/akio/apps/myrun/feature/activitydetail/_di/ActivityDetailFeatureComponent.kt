package akio.apps.myrun.feature.activitydetail._di

import akio.apps._base.di.ViewModelFactoryProvider
import akio.apps.base.wiring.DispatchersModule
import akio.apps.myrun.data.activity.wiring.ActivityDataComponent
import akio.apps.myrun.data.activity.wiring.DaggerActivityDataComponent
import akio.apps.myrun.data.authentication.wiring.AuthenticationDataComponent
import akio.apps.myrun.data.authentication.wiring.DaggerAuthenticationDataComponent
import akio.apps.myrun.data.recentplace.wiring.DaggerRecentPlaceDataComponent
import akio.apps.myrun.data.recentplace.wiring.RecentPlaceDataComponent
import akio.apps.myrun.feature.activitydetail.ActivityDetailViewModel
import android.app.Application
import dagger.BindsInstance
import dagger.Component

@Component(
    modules = [
        ActivityDetailFeatureModule::class,
        DispatchersModule::class
    ],
    dependencies = [
        ActivityDataComponent::class,
        AuthenticationDataComponent::class,
        RecentPlaceDataComponent::class
    ]
)
interface ActivityDetailFeatureComponent : ViewModelFactoryProvider {
    @Component.Factory
    interface Factory {
        fun create(
            @BindsInstance application: Application,
            @BindsInstance params: ActivityDetailViewModel.Params,
            activityDataComponent: ActivityDataComponent =
                DaggerActivityDataComponent.create(),
            authenticationDataComponent: AuthenticationDataComponent =
                DaggerAuthenticationDataComponent.create(),
            recentPlaceDataComponent: RecentPlaceDataComponent =
                DaggerRecentPlaceDataComponent.create()
        ): ActivityDetailFeatureComponent
    }
}
