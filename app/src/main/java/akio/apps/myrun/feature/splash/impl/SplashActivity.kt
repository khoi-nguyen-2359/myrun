package akio.apps.myrun.feature.splash.impl

import akio.apps._base.lifecycle.observeEvent
import akio.apps.myrun.R
import akio.apps.myrun._base.utils.DialogDelegate
import akio.apps.myrun._di.viewModel
import akio.apps.myrun.data.authentication.model.SignInSuccessResult
import akio.apps.myrun.feature.editprofile.impl.EditProfileActivity
import akio.apps.myrun.feature.home.HomeActivity
import akio.apps.myrun.feature.signin.impl.SignInActivity
import akio.apps.myrun.feature.splash.SplashViewModel
import akio.apps.myrun.feature.splash._di.DaggerSplashFeatureComponent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class SplashActivity : AppCompatActivity() {

    private val splashViewModel: SplashViewModel by viewModel {
        DaggerSplashFeatureComponent.create()
    }

    private val dialogDelegate by lazy { DialogDelegate(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        initObservers()
    }

    private fun initObservers() {
        observeEvent(splashViewModel.error, dialogDelegate::showExceptionAlert)
        observeEvent(splashViewModel.isUserSignedIn, ::onUserSignIn)
    }

    private fun onUserSignIn(isSignedIn: Boolean) {
        if (isSignedIn) {
            goHome()
        } else {
            @Suppress("DEPRECATION")
            startActivityForResult(SignInActivity.launchIntent(this), RC_SIGN_IN)
        }
    }

    @Suppress("DEPRECATION")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            RC_SIGN_IN -> verifySignInResult(resultCode, data)
            RC_ON_BOARDING -> goHome()
        }
    }

    private fun goHome() {
        startActivity(HomeActivity.clearTaskIntent(this))
    }

    private fun verifySignInResult(resultCode: Int, data: Intent?) {
        if (resultCode == RESULT_CANCELED) {
            finish()
            return
        }

        val signInResult =
            data?.getParcelableExtra<SignInSuccessResult>(SignInActivity.RESULT_SIGN_RESULT_DATA)
                ?: return

        if (signInResult.isNewUser) {
            @Suppress("DEPRECATION")
            startActivityForResult(
                EditProfileActivity.launchIntentForOnboarding(this),
                RC_ON_BOARDING
            )
        } else {
            goHome()
        }
    }

    companion object {
        const val RC_SIGN_IN = 1
        const val RC_ON_BOARDING = 2

        fun clearTaskIntent(context: Context) = Intent(context, SplashActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
        }
    }
}
