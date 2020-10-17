package akio.apps.myrun._di

import akio.apps.myrun.MyRunApp
import akio.apps.myrun.data.location._di.LocationDataModule
import akio.apps.myrun.data.routetracking._di.RouteTrackingDataModule
import akio.apps.myrun.data.activity._di.ActivityDataModule
import akio.apps.myrun.data.authentication.AuthenticationDataModule
import akio.apps.myrun.data.externalapp._di.ExternalAppDataModule
import akio.apps.myrun.data.place._di.PlaceDataModule
import akio.apps.myrun.data.recentplace._di.RecentPlaceDataModule
import akio.apps.myrun.data.userfollow._di.UserFollowDataModule
import akio.apps.myrun.data.userprofile._di.UserProfileDataModule
import akio.apps.myrun.feature.editprofile._di.EditProfileFeatureModule
import akio.apps.myrun.feature.strava._di.StravaConnectFeatureModule
import akio.apps.myrun.feature.usertimeline._di.UserTimelineFeatureModule
import akio.apps.myrun.feature.routetracking._di.RouteTrackingFeatureModule
import akio.apps.myrun.feature.signin._di.SignInFeatureModule
import akio.apps.myrun.feature.splash._di.SplashFeatureModule
import akio.apps.myrun.feature.userprofile._di.UserProfileFeatureModule
import dagger.BindsInstance
import dagger.Component
import dagger.android.AndroidInjectionModule
import dagger.android.AndroidInjector
import javax.inject.Singleton

@Singleton
@Component(modules = [
    AndroidInjectionModule::class,

    // app-scoped modules
    AppModule::class,
    GoogleMapApiModule::class,
    NetworkModule::class,

    // feature modules
    RouteTrackingFeatureModule::class,
    SplashFeatureModule::class,
    UserTimelineFeatureModule::class,
    SignInFeatureModule::class,
    UserProfileFeatureModule::class,
    StravaConnectFeatureModule::class,
    EditProfileFeatureModule::class,

    // data modules
    LocationDataModule::class,
    RouteTrackingDataModule::class,
    ActivityDataModule::class,
    AuthenticationDataModule::class,
    PlaceDataModule::class,
    RecentPlaceDataModule::class,
    UserFollowDataModule::class,
    UserProfileDataModule::class,
    ExternalAppDataModule::class
])
interface AppComponent: AndroidInjector<MyRunApp> {
    @Component.Factory
    interface Factory {
        fun create(@BindsInstance instance: MyRunApp): AppComponent
    }
}