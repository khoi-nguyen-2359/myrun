package akio.apps.myrun.domain.route.wiring

import akio.apps.myrun.data.location.wiring.DaggerLocationDataComponent
import akio.apps.myrun.data.location.wiring.LocationDataComponent
import akio.apps.myrun.domain.route.api.RoutePlottingUsecase
import akio.apps.myrun.domain.route.impl.RoutePlottingUsecaseImpl
import dagger.Binds
import dagger.Component
import dagger.Module

@Component(
    modules = [RouteDomainModule::class],
    dependencies = [LocationDataComponent::class]
)
interface RouteDomainComponent {
    fun routePlottingUsecase(): RoutePlottingUsecase

    @Component.Factory
    interface Factory {
        fun create(
            locationDataComponent: LocationDataComponent = DaggerLocationDataComponent.create()
        ): RouteDomainComponent
    }
}

@Module
internal interface RouteDomainModule {
    @Binds
    fun routePlottingUsecase(usecaseImpl: RoutePlottingUsecaseImpl): RoutePlottingUsecase
}
