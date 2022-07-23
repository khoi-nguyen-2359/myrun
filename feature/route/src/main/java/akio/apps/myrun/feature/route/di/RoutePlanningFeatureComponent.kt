package akio.apps.myrun.feature.route.di

import akio.apps.myrun.base.di.DispatchersModule
import akio.apps.myrun.base.di.FeatureScope
import akio.apps.myrun.data.location.di.DaggerLocationDataComponent
import akio.apps.myrun.data.location.di.LocationDataComponent
import akio.apps.myrun.data.route.DaggerRouteDataComponent
import akio.apps.myrun.data.route.RouteDataComponent
import akio.apps.myrun.feature.core.launchcatching.LaunchCatchingModule
import akio.apps.myrun.feature.route.RoutePlanningViewModel
import android.app.Application
import androidx.lifecycle.SavedStateHandle
import dagger.BindsInstance
import dagger.Component

@FeatureScope
@Component(
    modules = [
        LaunchCatchingModule::class,
        DispatchersModule::class
    ],
    dependencies = [
        RouteDataComponent::class,
        LocationDataComponent::class
    ]
)
internal interface RoutePlanningFeatureComponent {
    fun drawRouteViewModel(): RoutePlanningViewModel

    @Component.Factory
    interface Factory {
        fun create(
            @BindsInstance application: Application,
            @BindsInstance savedStateHandle: SavedStateHandle,
            routeDataComponent: RouteDataComponent = DaggerRouteDataComponent.create(),
            locationDataComponent: LocationDataComponent =
                DaggerLocationDataComponent.factory().create(application),
        ): RoutePlanningFeatureComponent
    }
}
