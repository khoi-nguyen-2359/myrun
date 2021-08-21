package akio.apps.myrun.feature.registration

import akio.apps.common.feature.lifecycle.collectEventRepeatOnStarted
import akio.apps.common.feature.lifecycle.collectRepeatOnStarted
import akio.apps.common.feature.viewmodel.lazyViewModelProvider
import akio.apps.myrun.data.authentication.api.model.SignInSuccessResult
import akio.apps.myrun.feature.base.DialogDelegate
import akio.apps.myrun.feature.registration.databinding.ActivitySignInBinding
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.auth.api.signin.GoogleSignInStatusCodes
import com.google.android.gms.common.api.ApiException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import timber.log.Timber

class SignInActivity : AppCompatActivity() {

    private val signInVM: SignInViewModel by lazyViewModelProvider {
        DaggerSignInFeatureComponent.factory().create().signInViewModel()
    }

    private val viewBinding: ActivitySignInBinding by lazy {
        ActivitySignInBinding.inflate(layoutInflater)
    }

    private val facebookCallbackManager by lazy { CallbackManager.Factory.create() }

    private val dialogDelegate by lazy { DialogDelegate(this) }

    private val fbCallback = object : FacebookCallback<LoginResult> {
        override fun onSuccess(result: LoginResult) {
            signInVM.signInWithFacebookToken(result.accessToken.token)
        }

        override fun onCancel() {
        }

        override fun onError(error: FacebookException?) {
            dialogDelegate.showErrorAlert(error?.message)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initViews()
        initObservers()
    }

    private fun initObservers() {
        collectEventRepeatOnStarted(signInVM.signInSuccessResult, ::onSignInSuccess)
        collectRepeatOnStarted(signInVM.isInProgress, dialogDelegate::toggleProgressDialog)
        collectEventRepeatOnStarted(signInVM.error, dialogDelegate::showExceptionAlert)
    }

    private fun initViews() {
        setContentView(viewBinding.root)

        viewBinding.apply {
            LoginManager.getInstance().registerCallback(facebookCallbackManager, fbCallback)
            facebookButton.setOnClickListener {
                LoginManager.getInstance()
                    .logInWithReadPermissions(this@SignInActivity, FB_LOGIN_PERMISSIONS)
            }

            googleButton.setOnClickListener {
                startGoogleSignIn()
            }
        }
    }

    private fun startGoogleSignIn() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.google_web_client_id))
            .requestEmail()
            .build()

        val googleSignInClient = GoogleSignIn.getClient(this, gso)
        val signInIntent = googleSignInClient.signInIntent
        @Suppress("DEPRECATION")
        startActivityForResult(signInIntent, RC_GOOGLE_SIGN_IN)
    }

    private fun onSignInSuccess(signInSuccessResult: SignInSuccessResult) {
        val resultIntent = Intent()
        resultIntent.putExtra(RESULT_SIGN_RESULT_DATA, signInSuccessResult)
        setResult(Activity.RESULT_OK, resultIntent)
        finish()
    }

    @Suppress("DEPRECATION")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        facebookCallbackManager.onActivityResult(requestCode, resultCode, data)
        super.onActivityResult(requestCode, resultCode, data)
        verifyGoogleSignInResult(requestCode, data)
    }

    private fun verifyGoogleSignInResult(requestCode: Int, data: Intent?) {
        if (requestCode == RC_GOOGLE_SIGN_IN) {
            lifecycleScope.launch {
                try {
                    val account = withContext(Dispatchers.IO) {
                        GoogleSignIn.getSignedInAccountFromIntent(data).await()
                    }
                    signInVM.signInWithGoogleToken(account.idToken!!)
                } catch (e: ApiException) {
                    Timber.d(e)
                    Toast.makeText(
                        this@SignInActivity,
                        GoogleSignInStatusCodes.getStatusCodeString(e.statusCode),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        LoginManager.getInstance()
            .unregisterCallback(facebookCallbackManager)
    }

    companion object {
        private val FB_LOGIN_PERMISSIONS = listOf("email", "public_profile")

        const val RESULT_SIGN_RESULT_DATA = "RESULT_SIGN_RESULT_DATA"

        private const val RC_GOOGLE_SIGN_IN = 1

        fun launchIntent(context: Context): Intent {
            return Intent(context, SignInActivity::class.java)
        }
    }
}
