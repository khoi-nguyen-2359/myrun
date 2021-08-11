package akio.apps.myrun.feature.splash.impl

import akio.apps.common.feature.lifecycle.collectEventRepeatOnStarted
import akio.apps.common.feature.viewmodel.viewModelProvider
import akio.apps.myrun.data.authentication.model.SignInSuccessResult
import akio.apps.myrun.feature.base.DialogDelegate
import akio.apps.myrun.feature.editprofile.impl.EditProfileActivity
import akio.apps.myrun.feature.home.HomeActivity
import akio.apps.myrun.feature.registration.SignInActivity
import akio.apps.myrun.feature.splash._di.DaggerSplashFeatureComponent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber

class SplashActivity : AppCompatActivity() {

    private val splashViewModel: SplashViewModel by viewModelProvider {
        DaggerSplashFeatureComponent.factory().create().splashViewModel()
    }

    private val dialogDelegate by lazy { DialogDelegate(this) }

    private val splashStartTime: Long = System.currentTimeMillis()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        installSplashScreen().setKeepVisibleCondition { true }
        initObservers()
    }

    private fun initObservers() {
        collectEventRepeatOnStarted(splashViewModel.error, dialogDelegate::showExceptionAlert)
        collectEventRepeatOnStarted(splashViewModel.isUserSignedIn, ::onUserSignIn)
    }

    private fun onUserSignIn(isSignedIn: Boolean) = lifecycleScope.launch {
        Timber.d("onUserSignIn $isSignedIn")
        val delayMore = SPLASH_MIN_SHOWTIME - (System.currentTimeMillis() - splashStartTime)
        if (delayMore > 0) {
            delay(delayMore)
        }
        if (isSignedIn) {
            goHome()
        } else {
            val intent = SignInActivity.launchIntent(this@SplashActivity)
            @Suppress("DEPRECATION")
            startActivityForResult(intent, RC_SIGN_IN)
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
        if (resultCode == RESULT_CANCELED || data == null) {
            finish()
            return
        }

        val signInResult = data.getParcelableExtra<SignInSuccessResult>(
            SignInActivity.RESULT_SIGN_RESULT_DATA
        ) ?: return

        if (signInResult.isNewUser) {
            @Suppress("DEPRECATION")
            startActivityForResult(
                EditProfileActivity.launchIntentForEditing(this),
                RC_ON_BOARDING
            )
        } else {
            goHome()
        }
    }

    companion object {
        private const val RC_SIGN_IN = 1
        private const val RC_ON_BOARDING = 2

        private const val SPLASH_MIN_SHOWTIME = 700

        fun clearTaskIntent(context: Context) = Intent(context, SplashActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
        }
    }
}
