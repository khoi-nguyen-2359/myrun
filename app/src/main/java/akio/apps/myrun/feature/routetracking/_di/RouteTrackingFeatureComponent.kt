package akio.apps.myrun.feature.routetracking._di

import akio.apps._base.di.AppDependantComponentFactory
import akio.apps._base.di.FeatureScope
import akio.apps._base.di.ViewModelFactoryProvider
import akio.apps.myrun._di.AppComponent
import akio.apps.myrun._di.DispatchersModule
import akio.apps.myrun.data.activity._di.ActivityDataModule
import akio.apps.myrun.data.activityfile._di.ActivityFileDataModule
import akio.apps.myrun.data.authentication._di.AuthenticationDataModule
import akio.apps.myrun.data.externalapp._di.ExternalAppDataModule
import akio.apps.myrun.data.fitness._di.FitnessDataModule
import akio.apps.myrun.data.location._di.LocationDataModule
import akio.apps.myrun.data.place._di.PlaceDataModule
import akio.apps.myrun.data.recentplace._di.RecentPlaceDataModule
import akio.apps.myrun.data.routetracking._di.RouteTrackingDataModule
import akio.apps.myrun.data.userprofile._di.UserProfileDataModule
import akio.apps.myrun.feature.routetracking.impl.RouteTrackingService
import akio.apps.myrun.feature.routetracking.impl.UpdateUserRecentPlaceWorker
import dagger.Component

@FeatureScope
@Component(
    modules = [
        RouteTrackingFeatureModule::class,
        RouteTrackingDataModule::class,
        ExternalAppDataModule::class,
        AuthenticationDataModule::class,
        LocationDataModule::class,
        ActivityDataModule::class,
        UserProfileDataModule::class,
        FitnessDataModule::class,
        ActivityFileDataModule::class,
        PlaceDataModule::class,
        RecentPlaceDataModule::class,
        DispatchersModule::class
    ],
    dependencies = [AppComponent::class]
)
interface RouteTrackingFeatureComponent : ViewModelFactoryProvider {
    fun inject(service: RouteTrackingService)
    fun inject(worker: UpdateUserRecentPlaceWorker)

    @Component.Factory
    interface Factory : AppDependantComponentFactory<RouteTrackingFeatureComponent>
}
