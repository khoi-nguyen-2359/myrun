package akio.apps.common.wiring

import android.app.Application
import dagger.Module
import dagger.Provides

/**
 * Module that provides application instance.
 */
@Module
object ApplicationModule {
    /**
     * Should be assigned value right at app start.
     */
    lateinit var application: Application

    @Provides
    fun application(): Application = application
}
