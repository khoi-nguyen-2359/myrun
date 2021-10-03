package akio.apps.myrun.domain.route.wiring

import akio.apps.myrun.data.location.wiring.DaggerLocationDataComponent
import akio.apps.myrun.data.location.wiring.LocationDataComponent
import akio.apps.myrun.domain.route.impl.RoutePlottingUsecase
import dagger.Component

@Component(
    dependencies = [LocationDataComponent::class]
)
interface RouteDomainComponent {
    fun routePlottingUsecase(): RoutePlottingUsecase

    @Component.Factory
    interface Factory {
        fun create(
            locationDataComponent: LocationDataComponent = DaggerLocationDataComponent.create(),
        ): RouteDomainComponent
    }
}
