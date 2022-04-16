package akio.apps.myrun.data.route

import akio.apps.myrun.data.route.api.RouteRepository
import dagger.Component

@Component(modules = [RouteDataModule::class])
interface RouteDataComponent {
    fun routeRepository(): RouteRepository
}
