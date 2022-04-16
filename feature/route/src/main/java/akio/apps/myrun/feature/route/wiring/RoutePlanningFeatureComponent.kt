package akio.apps.myrun.feature.route.wiring

import akio.apps.myrun.data.common.di.DispatchersModule
import akio.apps.myrun.data.location.LocationDataModule
import akio.apps.myrun.data.route.RouteDataComponent
import akio.apps.myrun.domain.launchcatching.LaunchCatchingModule
import akio.apps.myrun.feature.route.RoutePlanningViewModel
import android.app.Application
import androidx.lifecycle.SavedStateHandle
import dagger.BindsInstance
import dagger.Component

@Component(
    modules = [
        LaunchCatchingModule::class,
        LocationDataModule::class,
        DispatchersModule::class
    ],
    dependencies = [RouteDataComponent::class]
)
interface RoutePlanningFeatureComponent {
    fun drawRouteViewModel(): RoutePlanningViewModel

    @Component.Factory
    interface Factory {
        fun create(
            @BindsInstance application: Application,
            @BindsInstance savedStateHandle: SavedStateHandle,
            routeDataComponent: RouteDataComponent
        ): RoutePlanningFeatureComponent
    }
}
