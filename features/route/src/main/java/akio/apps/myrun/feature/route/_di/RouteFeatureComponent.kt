package akio.apps.myrun.feature.route._di

import akio.apps.common.wiring.LaunchCatchingModule
import akio.apps.myrun.data.location.wiring.DaggerLocationDataComponent
import akio.apps.myrun.data.location.wiring.LocationDataComponent
import akio.apps.myrun.domain.route.wiring.DaggerRouteDomainComponent
import akio.apps.myrun.domain.route.wiring.RouteDomainComponent
import akio.apps.myrun.feature.route.DrawRouteViewModel
import dagger.Component

@Component(
    dependencies = [
        LocationDataComponent::class,
        RouteDomainComponent::class
    ],
    modules = [LaunchCatchingModule::class]
)
interface RouteFeatureComponent {
    fun drawRouteViewModel(): DrawRouteViewModel

    @Component.Factory
    interface Factory {
        fun create(
            locationDataComponent: LocationDataComponent = DaggerLocationDataComponent.create(),
            routeDomainComponent: RouteDomainComponent =
                DaggerRouteDomainComponent.factory().create()
        ): RouteFeatureComponent
    }
}
