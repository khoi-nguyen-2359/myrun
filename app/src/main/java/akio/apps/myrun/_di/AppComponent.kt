package akio.apps.myrun._di

import akio.apps.myrun.MyRunApp
import akio.apps.myrun.data._base.NetworkModule
import akio.apps.myrun.data.activity._di.ActivityDataModule
import akio.apps.myrun.data.activityfile._di.ActivityFileDataModule
import akio.apps.myrun.data.authentication._di.AuthenticationDataModule
import akio.apps.myrun.data.externalapp._di.ExternalAppDataModule
import akio.apps.myrun.data.fitness._di.FitnessDataModule
import akio.apps.myrun.data.googlemap.GoogleMapApiModule
import akio.apps.myrun.data.location._di.LocationDataModule
import akio.apps.myrun.data.routetracking._di.RouteTrackingDataModule
import akio.apps.myrun.data.userfollow._di.UserFollowDataModule
import akio.apps.myrun.data.userprofile._di.UserProfileDataModule
import akio.apps.myrun.feature.editprofile._di.EditProfileFeatureModule
import akio.apps.myrun.feature.routetracking._di.RouteTrackingFeatureModule
import akio.apps.myrun.feature.signin._di.SignInFeatureModule
import akio.apps.myrun.feature.splash._di.SplashFeatureModule
import akio.apps.myrun.feature.strava._di.StravaFeatureModule
import akio.apps.myrun.feature.userprofile._di.UserProfileFeatureModule
import akio.apps.myrun.feature.usertimeline._di.UserTimelineFeatureModule
import dagger.BindsInstance
import dagger.Component
import dagger.android.AndroidInjectionModule
import dagger.android.support.AndroidSupportInjectionModule
import javax.inject.Singleton

@Singleton
@Component(
    modules = [
        AndroidInjectionModule::class,
        AndroidSupportInjectionModule::class,

        // app-scoped modules
        AppModule::class,
        GoogleMapApiModule::class,
        NetworkModule::class,

        // data modules
        RouteTrackingDataModule::class,
        ActivityFileDataModule::class,
        ExternalAppDataModule::class,
        AuthenticationDataModule::class,
        LocationDataModule::class,
        ActivityDataModule::class,
        UserProfileDataModule::class,
        FitnessDataModule::class,
        UserFollowDataModule::class,

        // feature modules
        RouteTrackingFeatureModule::class,
        SplashFeatureModule::class,
        StravaFeatureModule::class,
        UserProfileFeatureModule::class,
        SignInFeatureModule::class,
        UserTimelineFeatureModule::class,
        EditProfileFeatureModule::class
    ]
)
interface AppComponent {
    fun inject(myRunApp: MyRunApp)

    @Component.Factory
    interface Factory {
        fun create(@BindsInstance instance: MyRunApp): AppComponent
    }
}
