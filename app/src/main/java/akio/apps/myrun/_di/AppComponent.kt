package akio.apps.myrun._di

import akio.apps.myrun.MyRunApp
import akio.apps.myrun.data.location.LocationDataModule
import akio.apps.myrun.feature.routetracking.RouteTrackingFeatureModule
import dagger.BindsInstance
import dagger.Component
import dagger.android.AndroidInjectionModule
import dagger.android.AndroidInjector
import javax.inject.Singleton

@Singleton
@Component(modules = [
    AndroidInjectionModule::class,
    AppModule::class,
    RouteTrackingFeatureModule::class,
    LocationDataModule::class
])
interface AppComponent: AndroidInjector<MyRunApp> {
    @Component.Factory
    interface Factory {
        fun create(@BindsInstance instance: MyRunApp): AppComponent
    }
}