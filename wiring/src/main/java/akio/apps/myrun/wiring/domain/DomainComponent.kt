package akio.apps.myrun.wiring.domain

import akio.apps.common.wiring.ApplicationModule
import akio.apps.common.wiring.DispatchersModule
import akio.apps.myrun.data.wiring.FirebaseDataModule
import akio.apps.myrun.domain.activityexport.ExportTempTcxFileUsecase
import akio.apps.myrun.domain.authentication.PostSignInUsecase
import akio.apps.myrun.domain.recentplace.MakeActivityPlaceNameUsecase
import akio.apps.myrun.domain.recentplace.UpdateUserRecentPlaceUsecase
import akio.apps.myrun.domain.routetracking.ClearRouteTrackingStateUsecase
import akio.apps.myrun.domain.routetracking.GetTrackedLocationsUsecase
import akio.apps.myrun.domain.routetracking.StoreTrackingActivityDataUsecase
import akio.apps.myrun.domain.routetracking.UploadActivitiesUsecase
import akio.apps.myrun.domain.strava.DeauthorizeStravaUsecase
import akio.apps.myrun.domain.strava.ExchangeStravaLoginCodeUsecase
import akio.apps.myrun.domain.strava.RemoveStravaTokenUsecase
import akio.apps.myrun.domain.strava.UpdateStravaTokenUsecase
import akio.apps.myrun.domain.strava.UploadActivityFilesToStravaUsecase
import akio.apps.myrun.domain.user.GetProviderTokensUsecase
import akio.apps.myrun.domain.user.GetUserProfileUsecase
import akio.apps.myrun.domain.user.UpdateUserProfileUsecase
import akio.apps.myrun.domain.user.UploadUserAvatarImageUsecase
import akio.apps.myrun.domain.usertimeline.GetUserTimelineActivitiesUsecase
import akio.apps.myrun.wiring.data.activity.ActivityDataModule
import akio.apps.myrun.wiring.data.authentication.AuthenticationDataModule
import akio.apps.myrun.wiring.data.eapps.ExternalAppDataModule
import akio.apps.myrun.wiring.data.location.LocationDataModule
import akio.apps.myrun.wiring.data.tracking.TrackingDataModule
import akio.apps.myrun.wiring.data.user.UserDataModule
import dagger.Component

@Component(
    modules = [
        ApplicationModule::class,
        ActivityDataModule::class,
        DispatchersModule::class,
        FirebaseDataModule::class,
        AuthenticationDataModule::class,
        UserDataModule::class,
        ExternalAppDataModule::class,
        LocationDataModule::class,
        TrackingDataModule::class,
        AuthenticationDataModule::class,
        ExternalAppDataModule::class
    ]
)
interface DomainComponent {
    fun exportTempTcxFileUsecase(): ExportTempTcxFileUsecase
    fun postSignInUsecase(): PostSignInUsecase
    fun updateUserRecentPlaceUsecase(): UpdateUserRecentPlaceUsecase
    fun makeActivityPlaceNameUsecase(): MakeActivityPlaceNameUsecase
    fun clearRouteTrackingStateUsecase(): ClearRouteTrackingStateUsecase
    fun getTrackedLocationsUsecase(): GetTrackedLocationsUsecase
    fun storeTrackingActivityDataUsecase(): StoreTrackingActivityDataUsecase
    fun getUserTimelineActivitiesUsecase(): GetUserTimelineActivitiesUsecase
    fun uploadActivityFilesToStravaUsecase(): UploadActivityFilesToStravaUsecase
    fun uploadActivitiesUsecase(): UploadActivitiesUsecase
    fun exchangeStravaLoginCodeUsecase(): ExchangeStravaLoginCodeUsecase
    fun updateStravaTokenUsecase(): UpdateStravaTokenUsecase
    fun uploadUserAvatarImageUsecase(): UploadUserAvatarImageUsecase
    fun updateUserProfileUsecase(): UpdateUserProfileUsecase
    fun getUserProfileUsecase(): GetUserProfileUsecase
    fun deauthorizeStravaUsecase(): DeauthorizeStravaUsecase
    fun getProviderTokensUsecase(): GetProviderTokensUsecase
    fun removeStravaTokenUsecase(): RemoveStravaTokenUsecase
}