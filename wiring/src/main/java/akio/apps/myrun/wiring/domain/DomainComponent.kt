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
        TrackingDataModule::class
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
}
