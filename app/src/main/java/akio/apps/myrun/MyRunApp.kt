package akio.apps.myrun

import akio.apps._base.utils.CrashReportTree
import akio.apps.myrun._di.AppComponent
import akio.apps.myrun._di.DaggerAppComponent
import akio.apps.myrun.data.routetracking.RouteTrackingState
import akio.apps.myrun.data.routetracking.RouteTrackingStatus
import akio.apps.myrun.feature.routetracking.impl.RouteTrackingService
import akio.apps.myrun.feature.strava.RescheduleStravaUploadWorkerDelegate
import android.app.Application
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.work.Configuration
import com.google.android.libraries.places.api.Places
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasAndroidInjector
import kotlinx.coroutines.*
import timber.log.Timber
import javax.inject.Inject

class MyRunApp : Application(), LifecycleObserver, HasAndroidInjector, Configuration.Provider {

    @Inject
    lateinit var dispatchingAndroidInjector: DispatchingAndroidInjector<Any>

    @Inject
    lateinit var routeTrackingState: RouteTrackingState

    @Inject
    lateinit var rescheduleStravaUploadWorkerDelegate: RescheduleStravaUploadWorkerDelegate

    lateinit var appComponent: AppComponent
        private set

    private val exceptionHandler = CoroutineExceptionHandler { _, exception ->
        Timber.e(exception)
    }

    private val ioScope = CoroutineScope(SupervisorJob() + Dispatchers.IO + exceptionHandler)

    override fun onCreate() {
        super.onCreate()

        setupLogging()

        createAppComponent().inject(this)

        ProcessLifecycleOwner.get()
            .lifecycle
            .addObserver(this)

        initPlacesSdk()

        rescheduleStravaUploadWorker()
    }

    private fun rescheduleStravaUploadWorker() = ioScope.launch {
        rescheduleStravaUploadWorkerDelegate.rescheduleWorker()
    }

    override fun androidInjector(): AndroidInjector<Any> = dispatchingAndroidInjector

    private fun initPlacesSdk() {
        val apiKey = getString(R.string.google_maps_sdk_key)
        Places.initialize(applicationContext, apiKey)
    }

    private fun createAppComponent(): AppComponent {
        appComponent = DaggerAppComponent.factory()
            .create(this)
        return appComponent
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun onAppStarted() {
        ioScope.launch {
            if (routeTrackingState.getTrackingStatus() == RouteTrackingStatus.RESUMED &&
                !RouteTrackingService.isTrackingServiceRunning(this@MyRunApp)
            ) {
                withContext(Dispatchers.Main) {
                    startService(RouteTrackingService.resumeIntent(this@MyRunApp))
                }
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

    override fun getWorkManagerConfiguration(): Configuration {
        return if (BuildConfig.DEBUG) {
            Configuration.Builder()
                .setMinimumLoggingLevel(android.util.Log.DEBUG)
                .build()
        } else {
            Configuration.Builder()
                .setMinimumLoggingLevel(android.util.Log.ERROR)
                .build()
        }
    }
}
