package akio.apps.myrun.feature.main

import akio.apps.myrun.R
import akio.apps.myrun.data.activity.api.model.BaseActivityModel
import akio.apps.myrun.data.authentication.api.model.SignInSuccessResult
import akio.apps.myrun.feature.activitydetail.ActivityExportService
import akio.apps.myrun.feature.core.DialogDelegate
import akio.apps.myrun.feature.core.ktx.getParcelableExtraExt
import akio.apps.myrun.feature.core.ktx.lazyViewModelProvider
import akio.apps.myrun.feature.core.navigation.OnBoardingNavigation
import akio.apps.myrun.feature.main.di.DaggerMainActivityComponent
import akio.apps.myrun.feature.main.ui.MainNavHost
import akio.apps.myrun.feature.registration.SignInActivity
import akio.apps.myrun.feature.route.RoutePlanningFacade
import akio.apps.myrun.feature.tracking.LocationPermissionChecker
import akio.apps.myrun.feature.tracking.RouteTrackingActivity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber

class MainActivity : AppCompatActivity() {

    private val locationPermissionChecker: LocationPermissionChecker =
        LocationPermissionChecker(activity = this)

    private val mainViewModel: MainViewModel by lazyViewModelProvider {
        DaggerMainActivityComponent.factory().create(application).mainViewModel()
    }

    private val dialogDelegate by lazy { DialogDelegate(this) }

    private lateinit var splashScreen: SplashScreen
    private var splashStartTime: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        splashScreen = installSplashScreen().apply {
            setKeepOnScreenCondition { true }
        }
        splashStartTime = System.currentTimeMillis()
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        lifecycleScope.launch {
            mainViewModel.isUserSignedIn.collect(::onUserSignIn)
        }
        dialogDelegate.collectLaunchCatchingError(this, mainViewModel)
    }

    @Suppress("DEPRECATION")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            RC_SIGN_IN -> verifySignInResult(resultCode, data)
        }
    }

    private fun verifySignInResult(resultCode: Int, data: Intent?) {
        if (resultCode == RESULT_CANCELED || data == null) {
            finish()
            return
        }

        data.getParcelableExtraExt<SignInSuccessResult>(SignInActivity.RESULT_SIGN_RESULT_DATA)
            ?: return

        goHome()
    }

    private fun onUserSignIn(isSignedIn: Boolean) = lifecycleScope.launch {
        Timber.d("onUserSignIn $isSignedIn")
        val delayMore = SPLASH_MIN_SHOWTIME - (System.currentTimeMillis() - splashStartTime)
        if (delayMore > 0) {
            delay(delayMore)
        }
        if (isSignedIn) {
            splashScreen.setKeepOnScreenCondition { false }
            goHome()
        } else {
            val intent = OnBoardingNavigation.createSignInIntent(this@MainActivity)
                ?: return@launch
            @Suppress("DEPRECATION")
            startActivityForResult(intent, RC_SIGN_IN)
        }
    }

    private fun goHome() {
        setContent {
            MainNavHost(
                onClickFloatingActionButton = ::openRouteTrackingOrCheckRequiredPermission,
                onClickExportActivityFile = ::startActivityExportService
            ) { RoutePlanningFacade.startRoutePlanning(this@MainActivity) }
        }
    }

    private fun startActivityExportService(activity: BaseActivityModel) {
        val intent = ActivityExportService.createAddActivityIntent(
            this,
            ActivityExportService.ActivityInfo(
                activity.id,
                activity.name,
                activity.startTime
            )
        )
        ContextCompat.startForegroundService(this, intent)
    }

    // Check location permissions -> allow user to open tracking screen.
    private fun openRouteTrackingOrCheckRequiredPermission() {
        if (locationPermissionChecker.isGranted()) {
            openRouteTracking()
            return
        }

        lifecycleScope.launch {
            val missingRequiredPermission = !locationPermissionChecker.check()
            if (missingRequiredPermission) {
                Toast.makeText(
                    this@MainActivity,
                    R.string.location_permission_is_missing_error,
                    Toast.LENGTH_SHORT
                ).show()
                return@launch
            }

            openRouteTracking()
        }
    }

    private fun openRouteTracking() = startActivity(RouteTrackingActivity.launchIntent(this))

    companion object {
        fun clearTaskIntent(context: Context): Intent = Intent(context, MainActivity::class.java)
            .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)

        private const val SPLASH_MIN_SHOWTIME = 700
        private const val RC_SIGN_IN = 1
    }
}
