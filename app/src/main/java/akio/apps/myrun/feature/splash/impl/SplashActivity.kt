package akio.apps.myrun.feature.splash.impl

import akio.apps._base.lifecycle.observe
import akio.apps._base.lifecycle.observeEvent
import akio.apps.myrun.R
import akio.apps.myrun._di.createViewModelInjectionDelegate
import akio.apps.myrun.feature._base.utils.DialogDelegate
import akio.apps.myrun.feature.home.impl.HomeActivity
import akio.apps.myrun.feature.routetracking.impl.RouteTrackingActivity
import akio.apps.myrun.feature.signin.impl.SignInActivity
import akio.apps.myrun.feature.signin.impl.SignInSuccessResult
import akio.apps.myrun.feature.splash.SplashViewModel
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class SplashActivity: AppCompatActivity() {

    private val viewModelInjectionDelegate by lazy { createViewModelInjectionDelegate() }
    private val splashViewModel: SplashViewModel by lazy { viewModelInjectionDelegate.getViewModel() }

    private val dialogDelegate by lazy { DialogDelegate(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        observeEvent(splashViewModel.error, dialogDelegate::showExceptionAlert)

        observe(splashViewModel.isUserSignedIn) { isSignedIn ->
            if (!isSignedIn) {
                startActivityForResult(SignInActivity.launchIntent(this), RC_SIGN_IN)
            }
        }

        observe(splashViewModel.isRouteTrackingInProgress) { isTracking ->
            finish()
            if (isTracking) {
                startActivity(RouteTrackingActivity.launchIntent(this@SplashActivity))
            } else {
                startActivity(HomeActivity.launchIntent(this@SplashActivity))
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        verifySignInResult(requestCode, resultCode, data)
    }

    private fun verifySignInResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode != RC_SIGN_IN) {
            return
        }

        if (resultCode == RESULT_CANCELED) {
            finish()
            return
        }

        data?.getParcelableExtra<SignInSuccessResult>(SignInActivity.RESULT_SIGN_RESULT_DATA)
            ?: return

        startActivity(HomeActivity.clearTaskIntent(this))
    }

    companion object {
        const val RC_SIGN_IN = 1
    }
}