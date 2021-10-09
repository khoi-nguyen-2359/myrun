package akio.apps.myrun

import akio.apps.myrun.data.tracking.api.RouteTrackingState
import akio.apps.myrun.data.tracking.api.RouteTrackingStatus
import akio.apps.myrun.data.wiring.ApplicationModule
import akio.apps.myrun.feature.base.AppNotificationChannel
import akio.apps.myrun.feature.configurator.ConfiguratorGate
import akio.apps.myrun.feature.tracking.RouteTrackingService
import akio.apps.myrun.log.CrashReportTree
import akio.apps.myrun.log.MyDebugTree
import akio.apps.myrun.wiring.AppComponent
import akio.apps.myrun.wiring.DaggerAppComponent
import akio.apps.myrun.worker.AppMigrationWorker
import akio.apps.myrun.worker.UpdateUserRecentPlaceWorker
import android.app.Application
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.work.Configuration
import com.google.android.libraries.places.api.Places
import javax.inject.Inject
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber

class MyRunApp :
    Application(),
    DefaultLifecycleObserver,
    AppComponent.Holder,
    Configuration.Provider {

    @Inject
    lateinit var routeTrackingState: RouteTrackingState

    private lateinit var appComponent: AppComponent
    override fun getAppComponent(): AppComponent {
        if (!::appComponent.isInitialized) {
            appComponent = DaggerAppComponent.factory().create()
        }
        return appComponent
    }

    private val exceptionHandler = CoroutineExceptionHandler { _, exception ->
        Timber.e(exception)
    }

    private val ioScope = CoroutineScope(SupervisorJob() + Dispatchers.IO + exceptionHandler)

    override fun onCreate() {
        super<Application>.onCreate()

        ApplicationModule.application = this

        // create all notification channels at app startup.
        AppNotificationChannel.values().forEach { it.createChannelCompat(this) }

        setupLogging()

        getAppComponent().inject(this)

        ProcessLifecycleOwner.get().lifecycle.addObserver(this)

        initPlacesSdk()

        UpdateUserRecentPlaceWorker.enqueueDaily(this)

        ConfiguratorGate.notifyInDebugMode(this)

        AppMigrationWorker.enqueue(this)
    }

    private fun initPlacesSdk() {
        Places.initialize(applicationContext, getString(R.string.google_direction_api_key))
    }

    override fun onStart(owner: LifecycleOwner) {
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
