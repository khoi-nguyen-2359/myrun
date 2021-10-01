package akio.apps.myrun.feature.routetracking._di

import akio.apps.myrun.data.activity.wiring.ActivityDataComponent
import akio.apps.myrun.data.activity.wiring.DaggerActivityDataComponent
import akio.apps.myrun.data.authentication.wiring.AuthenticationDataComponent
import akio.apps.myrun.data.authentication.wiring.DaggerAuthenticationDataComponent
import akio.apps.myrun.data.location.wiring.DaggerLocationDataComponent
import akio.apps.myrun.data.location.wiring.LocationDataComponent
import akio.apps.myrun.data.wiring.ApplicationModule
import akio.apps.myrun.data.wiring.LaunchCatchingModule
import akio.apps.myrun.feature.routetracking.impl.RouteTrackingService
import akio.apps.myrun.feature.routetracking.impl.RouteTrackingViewModel
import akio.apps.myrun.wiring.data.eapps.DaggerExternalAppDataComponent
import akio.apps.myrun.wiring.data.eapps.ExternalAppDataComponent
import akio.apps.myrun.wiring.data.fitness.DaggerFitnessDataComponent
import akio.apps.myrun.wiring.data.fitness.FitnessDataComponent
import akio.apps.myrun.wiring.data.tracking.DaggerTrackingDataComponent
import akio.apps.myrun.wiring.data.tracking.TrackingDataComponent
import akio.apps.myrun.wiring.data.user.DaggerUserDataComponent
import akio.apps.myrun.wiring.data.user.UserDataComponent
import akio.apps.myrun.wiring.domain.DaggerDomainComponent
import akio.apps.myrun.wiring.domain.DomainComponent
import akio.apps.myrun.worker.ActivityUploadWorker
import akio.apps.myrun.worker.UpdateUserRecentPlaceWorker
import dagger.Component

@akio.apps.myrun.data.wiring.FeatureScope
@Component(
    modules = [
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
        LocationDataComponent::class,
        DomainComponent::class
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
            domainComponent: DomainComponent = DaggerDomainComponent.factory().create(),
        ): RouteTrackingFeatureComponent
    }
}
