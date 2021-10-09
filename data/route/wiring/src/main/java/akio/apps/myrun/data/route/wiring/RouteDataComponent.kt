package akio.apps.myrun.data.route.wiring

import akio.apps.myrun.data.route.api.RouteRepository
import akio.apps.myrun.data.route.impl.RouteRepositoryImpl
import dagger.Binds
import dagger.Component
import dagger.Module

@Component(modules = [RouteDataModule::class])
interface RouteDataComponent {
    fun routeRepository(): RouteRepository
}

@Module
internal interface RouteDataModule {
    @Binds
    fun routeRepository(impl: RouteRepositoryImpl): RouteRepository
}
