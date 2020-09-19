package akio.apps.myrun

import akio.apps._base.utils.CrashReportTree
import akio.apps.myrun._di.DaggerAppComponent
import android.app.Application
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasAndroidInjector
import timber.log.Timber
import javax.inject.Inject

class MyRunApp: Application(), HasAndroidInjector {

    @Inject
    lateinit var dispatchingAndroidInjector: DispatchingAndroidInjector<Any>

    override fun onCreate() {
        super.onCreate()

        setupLogging()

        DaggerAppComponent.factory().create(this).inject(this)
    }

    private fun setupLogging() {
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        } else {
            Timber.plant(CrashReportTree())
        }
    }

    override fun androidInjector(): AndroidInjector<Any> = dispatchingAndroidInjector
}