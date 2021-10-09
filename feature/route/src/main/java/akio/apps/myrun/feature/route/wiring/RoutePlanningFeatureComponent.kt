package akio.apps.myrun.feature.route.wiring

import akio.apps.myrun.data.location.wiring.DaggerLocationDataComponent
import akio.apps.myrun.data.location.wiring.LocationDataComponent
import akio.apps.myrun.data.route.wiring.DaggerRouteDataComponent
import akio.apps.myrun.data.route.wiring.RouteDataComponent
import akio.apps.myrun.data.wiring.LaunchCatchingModule
import akio.apps.myrun.domain.route.wiring.DaggerRouteDomainComponent
import akio.apps.myrun.domain.route.wiring.RouteDomainComponent
import akio.apps.myrun.feature.route.RoutePlanningViewModel
import androidx.lifecycle.SavedStateHandle
import dagger.BindsInstance
import dagger.Component

@Component(
    dependencies = [
        LocationDataComponent::class,
        RouteDomainComponent::class,
        RouteDataComponent::class
    ],
    modules = [LaunchCatchingModule::class]
)
interface RoutePlanningFeatureComponent {
    fun drawRouteViewModel(): RoutePlanningViewModel

    @Component.Factory
    interface Factory {
        fun create(
            @BindsInstance savedStateHandle: SavedStateHandle,
            locationDataComponent: LocationDataComponent = DaggerLocationDataComponent.create(),
            routeDomainComponent: RouteDomainComponent =
                DaggerRouteDomainComponent.factory().create(),
            routeDataComponent: RouteDataComponent = DaggerRouteDataComponent.create()
        ): RoutePlanningFeatureComponent
    }
}
