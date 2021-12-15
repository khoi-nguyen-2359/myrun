package akio.apps.myrun.data.route

import akio.apps.myrun.data.route.api.RouteRepository
import akio.apps.myrun.data.route.impl.RouteRepositoryImpl
import dagger.Binds
import dagger.Module

@Module
interface RouteDataModule {
    @Binds
    fun routeRepository(impl: RouteRepositoryImpl): RouteRepository
}
