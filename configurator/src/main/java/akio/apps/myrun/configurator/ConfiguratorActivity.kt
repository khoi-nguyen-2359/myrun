package akio.apps.myrun.configurator

import akio.apps.myrun.configurator._di.ConfiguratorComponent
import akio.apps.myrun.configurator._di.DaggerConfiguratorComponent
import akio.apps.myrun.configurator.ui.ConfiguratorScreen
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class ConfiguratorActivity : AppCompatActivity() {

    private val configuratorComponent: ConfiguratorComponent by lazy {
        DaggerConfiguratorComponent.factory().create(application)
    }

    private val viewModelProvider by lazy {
        ViewModelProvider(
            this@ConfiguratorActivity,
            object : ViewModelProvider.Factory {
                override fun <T : ViewModel?> create(modelClass: Class<T>): T = when (modelClass) {
                    RouteTrackingConfigurationViewModel::class.java ->
                        configuratorComponent.routeTrackingConfigurationViewModel() as T
                    else -> throw Exception("Invalid view model class")
                }
            }
        )
    }

    private val routeTrackingViewModel: RouteTrackingConfigurationViewModel by lazy {
        viewModelProvider[RouteTrackingConfigurationViewModel::class.java]
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        configuratorComponent.inject(this)
        setContent {
            ConfiguratorScreen(routeTrackingViewModel)
        }
    }
}
