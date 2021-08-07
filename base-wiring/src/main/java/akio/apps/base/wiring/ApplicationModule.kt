package akio.apps.base.wiring

import android.app.Application
import dagger.Module
import dagger.Provides

/**
 * Module that provides application instance that get from firebase app.
 */
@Module
object ApplicationModule {
    lateinit var application: Application

    @Provides
    fun application(): Application = application
}
