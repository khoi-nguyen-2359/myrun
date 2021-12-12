package akio.apps.myrun.domain.tracking.wiring

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
import akio.apps.myrun.data.wiring.DispatchersModule
import akio.apps.myrun.domain.activity.wiring.ActivityDomainComponent
import akio.apps.myrun.domain.activity.wiring.DaggerActivityDomainComponent
import akio.apps.myrun.domain.tracking.impl.ClearRouteTrackingStateUsecase
import akio.apps.myrun.domain.tracking.impl.GetTrackedLocationsUsecase
import akio.apps.myrun.domain.tracking.impl.StoreTrackingActivityDataUsecase
import akio.apps.myrun.domain.tracking.impl.UploadActivitiesUsecase
import dagger.Component

@Component(
    modules = [
        DispatchersModule::class
    ],
    dependencies = [
        TrackingDataComponent::class,
        LocationDataComponent::class,
        UserDataComponent::class,
        ActivityDomainComponent::class,
        AuthenticationDataComponent::class,
        ExternalAppDataComponent::class
    ]
)
interface TrackingDomainComponent {
    fun clearRouteTrackingStateUsecase(): ClearRouteTrackingStateUsecase
    fun getTrackedLocationsUsecase(): GetTrackedLocationsUsecase
    fun storeTrackingActivityDataUsecase(): StoreTrackingActivityDataUsecase
    fun uploadActivitiesUsecase(): UploadActivitiesUsecase

    @Component.Factory
    interface Factory {
        fun create(
            trackingDataComponent: TrackingDataComponent = DaggerTrackingDataComponent.create(),
            locationDataComponent: LocationDataComponent = DaggerLocationDataComponent.create(),
            userDataComponent: UserDataComponent = DaggerUserDataComponent.create(),
            activityDomainComponent: ActivityDomainComponent =
                DaggerActivityDomainComponent.factory().create(),
            authenticationDataComponent: AuthenticationDataComponent =
                DaggerAuthenticationDataComponent.create(),
            externalAppDataComponent: ExternalAppDataComponent =
                DaggerExternalAppDataComponent.factory().create()
        ): TrackingDomainComponent
    }
}
