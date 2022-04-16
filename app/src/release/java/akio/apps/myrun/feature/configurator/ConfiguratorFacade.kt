package akio.apps.myrun.feature.configurator

import android.content.Context

object ConfiguratorFacade {
    @Suppress("UNUSED_PARAMETER")
    fun notifyInDebugMode(context: Context) {
        // do not enable configurator in release build.
    }
}
