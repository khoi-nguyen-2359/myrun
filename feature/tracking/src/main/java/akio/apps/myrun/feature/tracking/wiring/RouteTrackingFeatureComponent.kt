package akio.apps.myrun.feature.tracking.wiring

import akio.apps.myrun.data.authentication.wiring.AuthenticationDataComponent
import akio.apps.myrun.data.authentication.wiring.DaggerAuthenticationDataComponent
import akio.apps.myrun.data.eapps.wiring.DaggerExternalAppDataComponent
import akio.apps.myrun.data.eapps.wiring.ExternalAppDataComponent
import akio.apps.myrun.data.location.wiring.DaggerLocationDataComponent
import akio.apps.myrun.data.location.wiring.LocationDataComponent
import akio.apps.myrun.data.tracking.wiring.DaggerTrackingDataComponent
import akio.apps.myrun.data.tracking.wiring.TrackingDataComponent
import akio.apps.myrun.data.user.wiring.DaggerUserDataComponent
import akio.apps.myrun.data.user.wiring.UserDataComponent
import akio.apps.myrun.data.wiring.ApplicationModule
import akio.apps.myrun.data.wiring.FeatureScope
import akio.apps.myrun.data.wiring.LaunchCatchingModule
import akio.apps.myrun.domain.activity.wiring.ActivityDomainComponent
import akio.apps.myrun.domain.activity.wiring.DaggerActivityDomainComponent
import akio.apps.myrun.domain.tracking.wiring.DaggerTrackingDomainComponent
import akio.apps.myrun.domain.tracking.wiring.TrackingDomainComponent
import akio.apps.myrun.feature.tracking.RouteTrackingService
import akio.apps.myrun.feature.tracking.RouteTrackingViewModel
import dagger.Component

@FeatureScope
@Component(
    modules = [
        ApplicationModule::class,
        LaunchCatchingModule::class
    ],
    dependencies = [
        ActivityDomainComponent::class,
        UserDataComponent::class,
        AuthenticationDataComponent::class,
        TrackingDataComponent::class,
        ExternalAppDataComponent::class,
        LocationDataComponent::class,
        TrackingDomainComponent::class,
    ]
)
interface RouteTrackingFeatureComponent {
    fun routeTrackingViewModel(): RouteTrackingViewModel

    fun inject(service: RouteTrackingService)

    @Component.Factory
    interface Factory {
        fun create(
            activityDomainComponent: ActivityDomainComponent =
                DaggerActivityDomainComponent.factory().create(),
            userDataComponent: UserDataComponent = DaggerUserDataComponent.create(),
            authenticationDataComponent: AuthenticationDataComponent =
                DaggerAuthenticationDataComponent.create(),
            trackingDataComponent: TrackingDataComponent = DaggerTrackingDataComponent.create(),
            locationDataComponent: LocationDataComponent = DaggerLocationDataComponent.create(),
            externalAppDataComponent: ExternalAppDataComponent =
                DaggerExternalAppDataComponent.factory().create(),
            trackingDomainComponent: TrackingDomainComponent =
                DaggerTrackingDomainComponent.factory().create(),
        ): RouteTrackingFeatureComponent
    }
}
