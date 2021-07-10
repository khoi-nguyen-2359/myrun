package akio.apps.myrun

import akio.apps._base.utils.CrashReportTree
import akio.apps._base.utils.MyDebugTree
import akio.apps.myrun._base.configurator.ConfiguratorGate
import akio.apps.myrun._base.notification.AppNotificationChannel
import akio.apps.myrun._di.AppComponent
import akio.apps.myrun._di.DaggerAppComponent
import akio.apps.myrun.data.routetracking.RouteTrackingState
import akio.apps.myrun.data.routetracking.RouteTrackingStatus
import akio.apps.myrun.feature.routetracking.impl.RouteTrackingService
import akio.apps.myrun.feature.routetracking.impl.UpdateUserRecentPlaceWorker
import android.app.Application
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.work.Configuration
import com.google.android.libraries.places.api.Places
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject

class MyRunApp : Application(), LifecycleObserver, AppComponent.Holder, Configuration.Provider {

    @Inject
    lateinit var routeTrackingState: RouteTrackingState

    private lateinit var appComponent: AppComponent
    override fun getAppComponent(): AppComponent {
        if (!::appComponent.isInitialized) {
            appComponent = DaggerAppComponent.factory().create(this)
        }
        return appComponent
    }

    private val exceptionHandler = CoroutineExceptionHandler { _, exception ->
        Timber.e(exception)
    }

    private val ioScope = CoroutineScope(SupervisorJob() + Dispatchers.IO + exceptionHandler)

    override fun onCreate() {
        super.onCreate()

        // create all notification channels at app startup.
        AppNotificationChannel.values().forEach { it.createChannelCompat(this) }

        setupLogging()

        getAppComponent().inject(this)

        ProcessLifecycleOwner.get().lifecycle.addObserver(this)

        initPlacesSdk()

        UpdateUserRecentPlaceWorker.enqueueDaily(this)

        ConfiguratorGate.notifyInDebugMode(this)
    }

    private fun initPlacesSdk() {
        Places.initialize(applicationContext, getString(R.string.google_api_key))
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
            Timber.plant(MyDebugTree(application = this))
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
