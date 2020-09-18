package akio.apps.myrun._di

import akio.apps.myrun.MyRunApp
import akio.apps.myrun.data.location.impl.LocationDataModule
import akio.apps.myrun.data.routetracking.impl.RouteTrackingDataModule
import akio.apps.myrun.data.workout.WorkoutDataModule
import akio.apps.myrun.feature.routetracking.impl.RouteTrackingFeatureModule
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

    // feature modules
    RouteTrackingFeatureModule::class,

    // data modules
    LocationDataModule::class,
    RouteTrackingDataModule::class,
    WorkoutDataModule::class,
])
interface AppComponent: AndroidInjector<MyRunApp> {
    @Component.Factory
    interface Factory {
        fun create(@BindsInstance instance: MyRunApp): AppComponent
    }
}