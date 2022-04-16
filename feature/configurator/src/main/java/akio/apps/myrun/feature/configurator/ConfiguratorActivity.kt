package akio.apps.myrun.feature.configurator

import akio.apps.myrun.feature.configurator.di.ConfiguratorComponent
import akio.apps.myrun.feature.configurator.di.DaggerConfiguratorComponent
import akio.apps.myrun.feature.configurator.ui.ConfiguratorScreen
import akio.apps.myrun.feature.configurator.viewmodel.RouteTrackingSectionViewModel
import akio.apps.myrun.feature.configurator.viewmodel.UserAuthenticationSectionViewModel
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity

class ConfiguratorActivity : AppCompatActivity() {

    private val configuratorComponent: ConfiguratorComponent by lazy {
        DaggerConfiguratorComponent.factory().create(application)
    }

    private val routeTrackingViewModel: RouteTrackingSectionViewModel by lazy {
        configuratorComponent.routeTrackingSectionViewModel()
    }

    private val userAuthSectionViewModel: UserAuthenticationSectionViewModel by lazy {
        configuratorComponent.userAuthenticationViewModel()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ConfiguratorScreen(
                routeTrackingViewModel,
                userAuthSectionViewModel
            )
        }
    }
}
