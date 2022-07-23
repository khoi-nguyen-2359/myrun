package akio.apps.myrun.data.route

import akio.apps.myrun.data.route.api.RouteRepository
import com.squareup.anvil.annotations.MergeComponent
import javax.inject.Singleton

abstract class RouteDataScope private constructor()

@Singleton
@MergeComponent(RouteDataScope::class)
interface RouteDataComponent {
    fun routeRepository(): RouteRepository
}
