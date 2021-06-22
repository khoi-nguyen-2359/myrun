package akio.apps.myrun.configurator

import akio.apps.myrun.configurator.ui.ConfiguratorScreen
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity

class ConfiguratorActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ConfiguratorScreen()
        }
    }
}
