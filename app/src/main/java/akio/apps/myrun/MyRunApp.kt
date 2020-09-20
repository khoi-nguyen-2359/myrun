package akio.apps.myrun

import akio.apps._base.utils.CrashReportTree
import akio.apps.myrun._di.DaggerAppComponent
import akio.apps.myrun.data.routetracking.RouteTrackingState
import akio.apps.myrun.data.routetracking.model.RouteTrackingStatus
import akio.apps.myrun.feature.routetracking.impl.RouteTrackingService
import android.app.Application
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.ProcessLifecycleOwner
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasAndroidInjector
import kotlinx.coroutines.*
import timber.log.Timber
import javax.inject.Inject

class MyRunApp : Application(), LifecycleObserver, HasAndroidInjector {

    @Inject
    lateinit var dispatchingAndroidInjector: DispatchingAndroidInjector<Any>

    @Inject
    lateinit var routeTrackingState: RouteTrackingState

    private val exceptionHandler = CoroutineExceptionHandler { context, exception ->
        Timber.e(exception)
    }

    private val mainScope = CoroutineScope(SupervisorJob() + Dispatchers.Main + exceptionHandler)

    override fun onCreate() {
        super.onCreate()

        setupLogging()

        DaggerAppComponent.factory().create(this).inject(this)

        ProcessLifecycleOwner.get()
            .lifecycle
            .addObserver(this)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun onAppStarted() {
        mainScope.launch {
            if (routeTrackingState.getTrackingStatus() == RouteTrackingStatus.RESUMED && !RouteTrackingService.isTrackingServiceRunning(this@MyRunApp)) {
                startService(RouteTrackingService.resumeIntent(this@MyRunApp))
            }
        }
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