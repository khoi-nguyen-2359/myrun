package akio.apps.myrun.feature.configuration.impl

import akio.apps.myrun.feature.configuration.impl._di.ConfiguratorComponent
import akio.apps.myrun.feature.configuration.impl._di.DaggerConfiguratorComponent
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
            akio.apps.myrun.feature.configuration.impl.ui.ConfiguratorScreen(routeTrackingViewModel)
        }
    }
}
