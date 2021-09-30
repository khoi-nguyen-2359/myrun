package akio.apps.myrun.wiring.domain

import akio.apps.common.wiring.ApplicationModule
import akio.apps.common.wiring.DispatchersModule
import akio.apps.myrun.data.activity.wiring.ActivityDataComponent
import akio.apps.myrun.data.activity.wiring.DaggerActivityDataComponent
import akio.apps.myrun.data.authentication.wiring.AuthenticationDataComponent
import akio.apps.myrun.data.authentication.wiring.DaggerAuthenticationDataComponent
import akio.apps.myrun.data.location.wiring.DaggerLocationDataComponent
import akio.apps.myrun.data.location.wiring.LocationDataComponent
import akio.apps.myrun.data.wiring.FirebaseDataModule
import akio.apps.myrun.domain.activity.ExportTempTcxFileUsecase
import akio.apps.myrun.domain.activity.GetFeedActivitiesUsecase
import akio.apps.myrun.domain.activity.GetTrainingSummaryDataUsecase
import akio.apps.myrun.domain.activity.RunSplitsCalculator
import akio.apps.myrun.domain.authentication.PostSignInUsecase
import akio.apps.myrun.domain.migration.AppMigrationUsecase
import akio.apps.myrun.domain.migration.task.MigrationTask10500
import akio.apps.myrun.domain.recentplace.GetUserRecentPlaceNameUsecase
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
import akio.apps.myrun.wiring.data.eapps.ExternalAppDataModule
import akio.apps.myrun.wiring.data.tracking.TrackingDataModule
import akio.apps.myrun.wiring.data.user.UserDataModule
import dagger.Component

@Component(
    modules = [
        ApplicationModule::class,
        DispatchersModule::class,
        FirebaseDataModule::class,
        UserDataModule::class,
        ExternalAppDataModule::class,
        TrackingDataModule::class,
        ExternalAppDataModule::class
    ],
    dependencies = [
        LocationDataComponent::class,
        ActivityDataComponent::class,
        AuthenticationDataComponent::class
    ]
)
interface DomainComponent {
    fun exportTempTcxFileUsecase(): ExportTempTcxFileUsecase
    fun postSignInUsecase(): PostSignInUsecase
    fun updateUserRecentPlaceUsecase(): UpdateUserRecentPlaceUsecase
    fun makeActivityPlaceNameUsecase(): MakeActivityPlaceNameUsecase
    fun getUserRecentPlaceUsecase(): GetUserRecentPlaceNameUsecase

    // tracking
    fun clearRouteTrackingStateUsecase(): ClearRouteTrackingStateUsecase
    fun getTrackedLocationsUsecase(): GetTrackedLocationsUsecase
    fun storeTrackingActivityDataUsecase(): StoreTrackingActivityDataUsecase

    // activity
    fun getUserTimelineActivitiesUsecase(): GetFeedActivitiesUsecase
    fun uploadActivityFilesToStravaUsecase(): UploadActivityFilesToStravaUsecase
    fun uploadActivitiesUsecase(): UploadActivitiesUsecase
    fun getTrainingSummaryUsecase(): GetTrainingSummaryDataUsecase

    // strava
    fun exchangeStravaLoginCodeUsecase(): ExchangeStravaLoginCodeUsecase
    fun updateStravaTokenUsecase(): UpdateStravaTokenUsecase

    // user usecases
    fun uploadUserAvatarImageUsecase(): UploadUserAvatarImageUsecase
    fun updateUserProfileUsecase(): UpdateUserProfileUsecase
    fun getUserProfileUsecase(): GetUserProfileUsecase

    fun deauthorizeStravaUsecase(): DeauthorizeStravaUsecase
    fun getProviderTokensUsecase(): GetProviderTokensUsecase
    fun removeStravaTokenUsecase(): RemoveStravaTokenUsecase
    fun runSplitCalculator(): RunSplitsCalculator

    // App Migration
    fun appVersionMigrationUsecase(): AppMigrationUsecase
    fun migrationTask10500(): MigrationTask10500

    @Component.Factory
    interface Factory {
        fun create(
            locationDataComponent: LocationDataComponent = DaggerLocationDataComponent.create(),
            activityDataComponent: ActivityDataComponent = DaggerActivityDataComponent.create(),
            authDataComponent: AuthenticationDataComponent =
                DaggerAuthenticationDataComponent.create()
        ): DomainComponent
    }
}
