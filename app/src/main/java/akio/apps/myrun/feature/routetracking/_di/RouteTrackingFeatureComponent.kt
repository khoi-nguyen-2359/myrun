package akio.apps.myrun.feature.routetracking._di

import akio.apps.base.feature.viewmodel.ViewModelFactoryProvider
import akio.apps.base.wiring.DispatchersModule
import akio.apps.base.wiring.FeatureScope
import akio.apps.myrun._di.AppComponent
import akio.apps.myrun.data.activity.wiring.ActivityDataComponent
import akio.apps.myrun.data.activity.wiring.DaggerActivityDataComponent
import akio.apps.myrun.data.authentication.wiring.AuthenticationDataComponent
import akio.apps.myrun.data.authentication.wiring.DaggerAuthenticationDataComponent
import akio.apps.myrun.data.external.wiring.DaggerExternalAppDataComponent
import akio.apps.myrun.data.external.wiring.ExternalAppDataComponent
import akio.apps.myrun.data.fitness.wiring.DaggerFitnessDataComponent
import akio.apps.myrun.data.fitness.wiring.FitnessDataComponent
import akio.apps.myrun.data.location.wiring.DaggerLocationDataComponent
import akio.apps.myrun.data.location.wiring.LocationDataComponent
import akio.apps.myrun.data.place.wiring.DaggerPlaceDataComponent
import akio.apps.myrun.data.place.wiring.PlaceDataComponent
import akio.apps.myrun.data.recentplace.wiring.DaggerRecentPlaceDataComponent
import akio.apps.myrun.data.recentplace.wiring.RecentPlaceDataComponent
import akio.apps.myrun.data.routetracking.wiring.DaggerRouteTrackingDataComponent
import akio.apps.myrun.data.routetracking.wiring.RouteTrackingDataComponent
import akio.apps.myrun.data.userprofile.wiring.DaggerUserProfileDataComponent
import akio.apps.myrun.data.userprofile.wiring.UserProfileDataComponent
import akio.apps.myrun.feature.routetracking.impl.ActivityUploadWorker
import akio.apps.myrun.feature.routetracking.impl.RouteTrackingService
import akio.apps.myrun.feature.routetracking.impl.UpdateUserRecentPlaceWorker
import android.app.Application
import dagger.BindsInstance
import dagger.Component

@FeatureScope
@Component(
    modules = [
        RouteTrackingFeatureModule::class,
        DispatchersModule::class
    ],
    dependencies = [
        AppComponent::class,
        ActivityDataComponent::class,
        UserProfileDataComponent::class,
        AuthenticationDataComponent::class,
        RecentPlaceDataComponent::class,
        RouteTrackingDataComponent::class,
        LocationDataComponent::class,
        FitnessDataComponent::class,
        PlaceDataComponent::class,
        ExternalAppDataComponent::class
    ]
)
interface RouteTrackingFeatureComponent : ViewModelFactoryProvider {
    fun inject(service: RouteTrackingService)
    fun inject(worker: UpdateUserRecentPlaceWorker)
    fun inject(worker: ActivityUploadWorker)

    @Component.Factory
    interface Factory {
        fun create(
            @BindsInstance application: Application,
            appComponent: AppComponent = (application as AppComponent.Holder).getAppComponent(),
            activityDataComponent: ActivityDataComponent = DaggerActivityDataComponent.create(),
            userProfileDataComponent: UserProfileDataComponent =
                DaggerUserProfileDataComponent.create(),
            authenticationDataComponent: AuthenticationDataComponent =
                DaggerAuthenticationDataComponent.create(),
            recentPlaceDataComponent: RecentPlaceDataComponent =
                DaggerRecentPlaceDataComponent.create(),
            routeTrackingDataComponent: RouteTrackingDataComponent =
                DaggerRouteTrackingDataComponent.create(),
            locationDataComponent: LocationDataComponent = DaggerLocationDataComponent.create(),
            fitnessDataComponent: FitnessDataComponent = DaggerFitnessDataComponent.create(),
            placeDataComponent: PlaceDataComponent = DaggerPlaceDataComponent.create(),
            externalAppDataComponent: ExternalAppDataComponent =
                DaggerExternalAppDataComponent.create()
        ): RouteTrackingFeatureComponent
    }
}
