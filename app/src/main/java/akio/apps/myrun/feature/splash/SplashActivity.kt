package akio.apps.myrun.feature.splash

import akio.apps.myrun.data.authentication.api.model.SignInSuccessResult
import akio.apps.myrun.feature.core.DialogDelegate
import akio.apps.myrun.feature.core.ktx.collectRepeatOnStarted
import akio.apps.myrun.feature.core.ktx.getParcelableExtraExt
import akio.apps.myrun.feature.core.ktx.lazyViewModelProvider
import akio.apps.myrun.feature.core.navigation.OnBoardingNavigation
import akio.apps.myrun.feature.main.MainActivity
import akio.apps.myrun.feature.registration.SignInActivity
import akio.apps.myrun.feature.splash.di.DaggerSplashFeatureComponent
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber

class SplashActivity : AppCompatActivity() {

    private val splashViewModel: SplashViewModel by lazyViewModelProvider {
        DaggerSplashFeatureComponent.factory().create(application).splashViewModel()
    }

    private val dialogDelegate by lazy { DialogDelegate(this) }

    private val splashStartTime: Long = System.currentTimeMillis()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        installSplashScreen().setKeepOnScreenCondition { true }
        initObservers()
    }

    private fun initObservers() {
        dialogDelegate.collectLaunchCatchingError(this, splashViewModel)
        collectRepeatOnStarted(splashViewModel.isUserSignedIn, ::onUserSignIn)
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
            val intent = OnBoardingNavigation.createSignInIntent(this@SplashActivity)
                ?: return@launch
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
        startActivity(MainActivity.clearTaskIntent(this))
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

    companion object {
        private const val RC_SIGN_IN = 1
        private const val RC_ON_BOARDING = 2

        private const val SPLASH_MIN_SHOWTIME = 700
    }
}
