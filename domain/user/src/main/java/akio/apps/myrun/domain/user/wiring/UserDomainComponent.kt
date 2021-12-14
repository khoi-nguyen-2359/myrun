package akio.apps.myrun.domain.user.wiring

import akio.apps.myrun.data.authentication.wiring.AuthenticationDataComponent
import akio.apps.myrun.data.authentication.wiring.DaggerAuthenticationDataComponent
import akio.apps.myrun.data.eapps.wiring.DaggerExternalAppDataComponent
import akio.apps.myrun.data.eapps.wiring.ExternalAppDataComponent
import akio.apps.myrun.data.location.wiring.DaggerLocationDataComponent
import akio.apps.myrun.data.location.wiring.LocationDataComponent
import akio.apps.myrun.data.user.wiring.DaggerUserDataComponent
import akio.apps.myrun.data.user.wiring.UserDataComponent
import akio.apps.myrun.data.wiring.ApplicationModule
import akio.apps.myrun.data.wiring.DispatchersModule
import akio.apps.myrun.domain.activity.wiring.ActivityDomainComponent
import akio.apps.myrun.domain.activity.wiring.DaggerActivityDomainComponent
import akio.apps.myrun.domain.tracking.wiring.DaggerTrackingDomainComponent
import akio.apps.myrun.domain.tracking.wiring.TrackingDomainComponent
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
        ActivityDomainComponent::class,
        UserDataComponent::class,
        AuthenticationDataComponent::class,
        ExternalAppDataComponent::class,
        LocationDataComponent::class,
        TrackingDomainComponent::class
    ],
    modules = [
        DispatchersModule::class,
        ApplicationModule::class
    ]
)
interface UserDomainComponent {
    fun getProviderTokensUsecase(): GetProviderTokensUsecase
    fun getTrainingSummaryDataUsecase(): GetTrainingSummaryDataUsecase
    fun getUserProfileUsecase(): GetUserProfileUsecase
    fun postSignInUsecase(): PostSignInUsecase
    fun updateUserProfileUsecase(): UpdateUserProfileUsecase
    fun uploadUserAvatarImageUsecase(): UploadUserAvatarImageUsecase
    fun getUserRecentPlaceNameUsecase(): GetUserRecentPlaceNameUsecase
    fun placeNameSelector(): PlaceNameSelector
    fun updateUserRecentPlaceUsecase(): UpdateUserRecentPlaceUsecase

    @Component.Factory
    interface Factory {
        fun create(
            externalAppDataComponent: ExternalAppDataComponent =
                DaggerExternalAppDataComponent.factory().create(),
            activityDataComponent: ActivityDomainComponent =
                DaggerActivityDomainComponent.factory().create(),
            userDataComponent: UserDataComponent = DaggerUserDataComponent.create(),
            authenticationDataComponent: AuthenticationDataComponent =
                DaggerAuthenticationDataComponent.create(),
            locationDataComponent: LocationDataComponent = DaggerLocationDataComponent.create(),
            trackingDataComponent: TrackingDomainComponent = DaggerTrackingDomainComponent.factory()
                .create(),
        ): UserDomainComponent
    }
}
