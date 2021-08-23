package akio.apps.myrun.feature.routetracking._di

import akio.apps.common.wiring.ApplicationModule
import akio.apps.common.wiring.DispatchersModule
import akio.apps.common.wiring.FeatureScope
import akio.apps.common.wiring.LaunchCatchingModule
import akio.apps.myrun.feature.routetracking.impl.RouteTrackingService
import akio.apps.myrun.feature.routetracking.impl.RouteTrackingViewModel
import akio.apps.myrun.wiring.data.activity.ActivityDataComponent
import akio.apps.myrun.wiring.data.activity.DaggerActivityDataComponent
import akio.apps.myrun.wiring.data.authentication.AuthenticationDataComponent
import akio.apps.myrun.wiring.data.authentication.DaggerAuthenticationDataComponent
import akio.apps.myrun.wiring.data.eapps.DaggerExternalAppDataComponent
import akio.apps.myrun.wiring.data.eapps.ExternalAppDataComponent
import akio.apps.myrun.wiring.data.fitness.DaggerFitnessDataComponent
import akio.apps.myrun.wiring.data.fitness.FitnessDataComponent
import akio.apps.myrun.wiring.data.location.DaggerLocationDataComponent
import akio.apps.myrun.wiring.data.location.LocationDataComponent
import akio.apps.myrun.wiring.data.tracking.DaggerTrackingDataComponent
import akio.apps.myrun.wiring.data.tracking.TrackingDataComponent
import akio.apps.myrun.wiring.data.user.DaggerUserDataComponent
import akio.apps.myrun.wiring.data.user.UserDataComponent
import akio.apps.myrun.worker.ActivityUploadWorker
import akio.apps.myrun.worker.UpdateUserRecentPlaceWorker
import dagger.Component

@FeatureScope
@Component(
    modules = [
        DispatchersModule::class,
        ApplicationModule::class,
        LaunchCatchingModule::class
    ],
    dependencies = [
        ActivityDataComponent::class,
        UserDataComponent::class,
        AuthenticationDataComponent::class,
        TrackingDataComponent::class,
        FitnessDataComponent::class,
        ExternalAppDataComponent::class,
        LocationDataComponent::class
    ]
)
interface RouteTrackingFeatureComponent {
    fun routeTrackingViewModel(): RouteTrackingViewModel

    fun inject(service: RouteTrackingService)
    fun inject(worker: UpdateUserRecentPlaceWorker)
    fun inject(worker: ActivityUploadWorker)

    @Component.Factory
    interface Factory {
        fun create(
            activityDataComponent: ActivityDataComponent = DaggerActivityDataComponent.create(),
            userDataComponent: UserDataComponent = DaggerUserDataComponent.create(),
            authenticationDataComponent: AuthenticationDataComponent =
                DaggerAuthenticationDataComponent.create(),
            trackingDataComponent: TrackingDataComponent = DaggerTrackingDataComponent.create(),
            locationDataComponent: LocationDataComponent = DaggerLocationDataComponent.create(),
            fitnessDataComponent: FitnessDataComponent = DaggerFitnessDataComponent.create(),
            externalAppDataComponent: ExternalAppDataComponent =
                DaggerExternalAppDataComponent.factory().create(),
        ): RouteTrackingFeatureComponent
    }
}
