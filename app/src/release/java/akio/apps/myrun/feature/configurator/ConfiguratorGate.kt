package akio.apps.myrun.feature.configurator

import android.content.Context

object ConfiguratorGate {
    @Suppress("UNUSED_PARAMETER")
    fun notifyInDebugMode(context: Context) {
        // do no enable configurator in release build.
    }
}
