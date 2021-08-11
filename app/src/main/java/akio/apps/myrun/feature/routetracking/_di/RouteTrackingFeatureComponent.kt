package akio.apps.myrun.feature.routetracking._di

import akio.apps.common.feature.viewmodel.ViewModelFactoryProvider
import akio.apps.common.wiring.ApplicationModule
import akio.apps.common.wiring.DispatchersModule
import akio.apps.common.wiring.FeatureScope
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
import akio.apps.myrun.data.tracking.wiring.DaggerTrackingDataComponent
import akio.apps.myrun.data.tracking.wiring.TrackingDataComponent
import akio.apps.myrun.data.user.wiring.DaggerUserDataComponent
import akio.apps.myrun.data.user.wiring.UserDataComponent
import akio.apps.myrun.feature.routetracking.impl.ActivityUploadWorker
import akio.apps.myrun.feature.routetracking.impl.RouteTrackingService
import akio.apps.myrun.feature.routetracking.impl.UpdateUserRecentPlaceWorker
import dagger.Component

@FeatureScope
@Component(
    modules = [
        RouteTrackingFeatureModule::class,
        DispatchersModule::class,
        ApplicationModule::class
    ],
    dependencies = [
        ActivityDataComponent::class,
        akio.apps.myrun.data.user.wiring.UserDataComponent::class,
        AuthenticationDataComponent::class,
        TrackingDataComponent::class,
        FitnessDataComponent::class,
        ExternalAppDataComponent::class,
        LocationDataComponent::class
    ]
)
interface RouteTrackingFeatureComponent : ViewModelFactoryProvider {
    fun inject(service: RouteTrackingService)
    fun inject(worker: UpdateUserRecentPlaceWorker)
    fun inject(worker: ActivityUploadWorker)

    @Component.Factory
    interface Factory {
        fun create(
            activityDataComponent: ActivityDataComponent = DaggerActivityDataComponent.create(),
            userDataComponent: akio.apps.myrun.data.user.wiring.UserDataComponent = DaggerUserDataComponent.create(),
            authenticationDataComponent: AuthenticationDataComponent =
                DaggerAuthenticationDataComponent.create(),
            trackingDataComponent: TrackingDataComponent = DaggerTrackingDataComponent.create(),
            locationDataComponent: LocationDataComponent = DaggerLocationDataComponent.create(),
            fitnessDataComponent: FitnessDataComponent = DaggerFitnessDataComponent.create(),
            externalAppDataComponent: ExternalAppDataComponent =
                DaggerExternalAppDataComponent.create()
        ): RouteTrackingFeatureComponent
    }
}
