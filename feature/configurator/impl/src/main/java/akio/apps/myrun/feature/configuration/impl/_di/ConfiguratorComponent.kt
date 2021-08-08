package akio.apps.myrun.feature.configuration.impl._di

import akio.apps.myrun.data.routetracking.wiring.DaggerRouteTrackingDataComponent
import akio.apps.myrun.data.routetracking.wiring.RouteTrackingDataComponent
import akio.apps.myrun.feature.configuration.impl.ConfiguratorActivity
import akio.apps.myrun.feature.configuration.impl.RouteTrackingConfigurationViewModel
import android.app.Application
import dagger.BindsInstance
import dagger.Component

@Component(
    dependencies = [RouteTrackingDataComponent::class]
)
interface ConfiguratorComponent {

    fun inject(configuratorActivity: ConfiguratorActivity)
    fun routeTrackingConfigurationViewModel(): RouteTrackingConfigurationViewModel

    @Component.Factory
    interface Factory {
        fun create(
            @BindsInstance application: Application,
            routeTrackingDataComponent: RouteTrackingDataComponent =
                DaggerRouteTrackingDataComponent.create()
        ): ConfiguratorComponent
    }
}
