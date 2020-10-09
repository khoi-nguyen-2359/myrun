package akio.apps.myrun._di

import akio.apps.myrun.MyRunApp
import akio.apps.myrun.data.location._di.LocationDataModule
import akio.apps.myrun.data.routetracking._di.RouteTrackingDataModule
import akio.apps.myrun.data.activity._di.ActivityDataModule
import akio.apps.myrun.data.authentication.AuthenticationDataModule
import akio.apps.myrun.data.place._di.PlaceDataModule
import akio.apps.myrun.data.recentplace._di.RecentPlaceDataModule
import akio.apps.myrun.feature.usertimeline._di.UserTimelineFeatureModule
import akio.apps.myrun.feature.routetracking._di.RouteTrackingFeatureModule
import akio.apps.myrun.feature.signin.SignInFeatureModule
import akio.apps.myrun.feature.splash._di.SplashFeatureModule
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

    // data modules
    LocationDataModule::class,
    RouteTrackingDataModule::class,
    ActivityDataModule::class,
    AuthenticationDataModule::class,
    PlaceDataModule::class,
    RecentPlaceDataModule::class
])
interface AppComponent: AndroidInjector<MyRunApp> {
    @Component.Factory
    interface Factory {
        fun create(@BindsInstance instance: MyRunApp): AppComponent
    }

    fun inject(viewModelInjectionDelegate: ViewModelInjectionDelegate)
}