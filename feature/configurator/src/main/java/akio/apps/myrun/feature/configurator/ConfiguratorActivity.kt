package akio.apps.myrun.feature.configurator

import akio.apps.myrun.feature.configurator._di.ConfiguratorComponent
import akio.apps.myrun.feature.configurator._di.DaggerConfiguratorComponent
import akio.apps.myrun.feature.configurator.ui.ConfiguratorScreen
import akio.apps.myrun.feature.configurator.viewmodel.UserAuthenticationViewModel
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

    private val userAuthViewModel: UserAuthenticationViewModel by lazy {
        configuratorComponent.userAuthenticationViewModel()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ConfiguratorScreen(
                routeTrackingViewModel,
                userAuthViewModel
            )
        }
    }
}
