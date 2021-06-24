package akio.apps.myrun.configurator._di

import akio.apps.myrun.configurator.ConfiguratorActivity
import akio.apps.myrun.configurator.RouteTrackingConfigurationViewModel
import akio.apps.myrun.data.routetracking._di.RouteTrackingDataModule
import android.app.Application
import dagger.BindsInstance
import dagger.Component

@Component(
    modules = [
        RouteTrackingDataModule::class
    ]
)
interface ConfiguratorComponent {

    fun inject(configuratorActivity: ConfiguratorActivity)
    fun routeTrackingConfigurationViewModel(): RouteTrackingConfigurationViewModel

    @Component.Factory
    interface Factory {
        fun create(@BindsInstance application: Application): ConfiguratorComponent
    }
}
