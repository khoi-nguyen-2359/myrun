package akio.apps.myrun

import akio.apps.myrun.data.tracking.api.RouteTrackingState
import akio.apps.myrun.data.tracking.api.model.RouteTrackingStatus
import akio.apps.myrun.di.AppComponent
import akio.apps.myrun.di.DaggerAppComponent
import akio.apps.myrun.feature.configurator.ConfiguratorFacade
import akio.apps.myrun.feature.core.AppNotificationChannel
import akio.apps.myrun.feature.tracking.RouteTrackingService
import akio.apps.myrun.log.CrashReportTree
import akio.apps.myrun.log.MyDebugTree
import akio.apps.myrun.worker.AppMigrationWorker
import akio.apps.myrun.worker.UpdateUserRecentPlaceWorker
import android.app.Application
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.work.Configuration
import com.google.android.gms.maps.MapsInitializer
import com.google.android.gms.maps.OnMapsSdkInitializedCallback
import com.google.android.libraries.places.api.Places
import com.google.firebase.FirebaseApp
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory
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
    Configuration.Provider,
    OnMapsSdkInitializedCallback {

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
        super<Application>.onCreate()

        initFirebase()

        // create all notification channels at app startup.
        AppNotificationChannel.values().forEach { it.createChannelCompat(this) }

        setupLogging()

        getAppComponent().inject(this)

        ProcessLifecycleOwner.get().lifecycle.addObserver(this)

        initPlacesSdk()

        UpdateUserRecentPlaceWorker.enqueueDaily(this)

        ConfiguratorFacade.notifyInDebugMode(this)

        AppMigrationWorker.enqueue(this)

        MapsInitializer.initialize(this, MapsInitializer.Renderer.LATEST, this)
    }

    private fun initFirebase() {
        FirebaseApp.initializeApp(this/* context */)
        val firebaseAppCheck = FirebaseAppCheck.getInstance()
        firebaseAppCheck.installAppCheckProviderFactory(
            if (BuildConfig.DEBUG) {
                DebugAppCheckProviderFactory.getInstance()
            } else {
                PlayIntegrityAppCheckProviderFactory.getInstance()
            }
        )
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

    override fun onMapsSdkInitialized(renderer: MapsInitializer.Renderer) {
        when (renderer) {
            MapsInitializer.Renderer.LATEST ->
                Timber.d("MapsDemo", "The latest version of the renderer is used.")
            MapsInitializer.Renderer.LEGACY ->
                Timber.d("MapsDemo", "The legacy version of the renderer is used.")
        }
    }
}
