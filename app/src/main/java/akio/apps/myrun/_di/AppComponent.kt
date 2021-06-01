package akio.apps.myrun._di

import akio.apps.myrun.MyRunApp
import akio.apps.myrun.data.activitysharing._di.ActivitySharingDataModule
import akio.apps.myrun.data.authentication._di.AuthenticationDataModule
import akio.apps.myrun.data.externalapp._di.ExternalAppDataModule
import akio.apps.myrun.data.externalapp.impl.StravaApi
import akio.apps.myrun.data.routetracking.RouteTrackingState
import akio.apps.myrun.data.routetracking._di.RouteTrackingDataModule
import akio.apps.myrun.feature.strava._di.StravaFeatureModule
import android.app.Application
import dagger.BindsInstance
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(
    modules = [
        // singleton modules
        ExternalAppDataModule.StravaApiDataModule::class,
        RouteTrackingDataModule.RouteTrackingStateDataModule::class,

        // application injection modules
        ExternalAppDataModule::class,
        StravaFeatureModule::class,
        AuthenticationDataModule::class,
        ActivitySharingDataModule::class,
        DispatchersModule::class
    ]
)
interface AppComponent {
    // Application injection method
    fun inject(myRunApp: MyRunApp)

    // Singleton provision methods
    fun routeTrackingState(): RouteTrackingState
    fun stravaApi(): StravaApi

    @Component.Factory
    interface Factory {
        fun create(@BindsInstance application: Application): AppComponent
    }

    interface Holder {
        fun getAppComponent(): AppComponent
    }
}
