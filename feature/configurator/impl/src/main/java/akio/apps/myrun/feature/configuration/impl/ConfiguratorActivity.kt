package akio.apps.myrun.feature.configuration.impl

import akio.apps.myrun.feature.configuration.impl._di.ConfiguratorComponent
import akio.apps.myrun.feature.configuration.impl._di.DaggerConfiguratorComponent
import akio.apps.myrun.feature.configuration.impl.ui.ConfiguratorScreen
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity

class ConfiguratorActivity : AppCompatActivity() {

    private val configuratorComponent: ConfiguratorComponent by lazy {
        DaggerConfiguratorComponent.factory().create()
    }

    private val routeTrackingViewModel: RouteTrackingConfigurationViewModel by lazy {
        configuratorComponent.routeTrackingConfigurationViewModel()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ConfiguratorScreen(routeTrackingViewModel)
        }
    }
}
