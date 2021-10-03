package akio.apps.myrun.domain.user.wiring

import akio.apps.myrun.data.activity.wiring.ActivityDataComponent
import akio.apps.myrun.data.activity.wiring.DaggerActivityDataComponent
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
import akio.apps.myrun.data.wiring.DispatchersModule
import akio.apps.myrun.domain.user.impl.GetProviderTokensUsecase
import akio.apps.myrun.domain.user.impl.GetTrainingSummaryDataUsecase
import akio.apps.myrun.domain.user.impl.GetUserProfileUsecase
import akio.apps.myrun.domain.user.impl.GetUserRecentPlaceNameUsecase
import akio.apps.myrun.domain.user.impl.PlaceNameSelector
import akio.apps.myrun.domain.user.impl.PostSignInUsecase
import akio.apps.myrun.domain.user.impl.UpdateUserProfileUsecase
import akio.apps.myrun.domain.user.impl.UpdateUserRecentPlaceUsecase
import akio.apps.myrun.domain.user.impl.UploadUserAvatarImageUsecase
import dagger.Component

@Component(
    dependencies = [
        ActivityDataComponent::class,
        UserDataComponent::class,
        AuthenticationDataComponent::class,
        ExternalAppDataComponent::class,
        LocationDataComponent::class,
        TrackingDataComponent::class
    ],
    modules = [
        DispatchersModule::class,
        ApplicationModule::class
    ]
)
interface UserDomainComponent {
    fun GetProviderTokensUsecase(): GetProviderTokensUsecase
    fun GetTrainingSummaryDataUsecase(): GetTrainingSummaryDataUsecase
    fun GetUserProfileUsecase(): GetUserProfileUsecase
    fun PostSignInUsecase(): PostSignInUsecase
    fun UpdateUserProfileUsecase(): UpdateUserProfileUsecase
    fun UploadUserAvatarImageUsecase(): UploadUserAvatarImageUsecase
    fun GetUserRecentPlaceNameUsecase(): GetUserRecentPlaceNameUsecase
    fun PlaceNameSelector(): PlaceNameSelector
    fun UpdateUserRecentPlaceUsecase(): UpdateUserRecentPlaceUsecase

    @Component.Factory
    interface Factory {
        fun create(
            externalAppDataComponent: ExternalAppDataComponent =
                DaggerExternalAppDataComponent.factory().create(),
            activityDataComponent: ActivityDataComponent = DaggerActivityDataComponent.create(),
            userDataComponent: UserDataComponent = DaggerUserDataComponent.create(),
            authenticationDataComponent: AuthenticationDataComponent =
                DaggerAuthenticationDataComponent.create(),
            locationDataComponent: LocationDataComponent = DaggerLocationDataComponent.create(),
            trackingDataComponent: TrackingDataComponent = DaggerTrackingDataComponent.create()
        ): UserDomainComponent
    }
}
