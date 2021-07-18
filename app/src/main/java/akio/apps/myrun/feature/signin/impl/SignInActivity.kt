package akio.apps.myrun.feature.signin.impl

import akio.apps._base.lifecycle.observe
import akio.apps._base.lifecycle.observeEvent
import akio.apps.myrun.R
import akio.apps.myrun._base.utils.DialogDelegate
import akio.apps.myrun._di.viewModel
import akio.apps.myrun.data.authentication.model.SignInSuccessResult
import akio.apps.myrun.databinding.ActivitySignInBinding
import akio.apps.myrun.feature.signin.SignInViewModel
import akio.apps.myrun.feature.signin._di.DaggerSignInFeatureComponent
import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class SignInActivity : AppCompatActivity() {

    private val signInVM: SignInViewModel by viewModel {
        DaggerSignInFeatureComponent.factory().create(applicationContext as Application)
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
        observeEvent(signInVM.signInSuccessResult, ::onSignInSuccess)
        observe(signInVM.isInProgress, dialogDelegate::toggleProgressDialog)
        observeEvent(signInVM.error, dialogDelegate::showExceptionAlert)
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
            .requestIdToken(getString(R.string.default_web_client_id))
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
                        GoogleSignIn.getSignedInAccountFromIntent(data)
                            .await()
                    }
                    signInVM.signInWithGoogleToken(account.idToken!!)
                } catch (e: ApiException) {
                    dialogDelegate.showExceptionAlert(e)
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
        val FB_LOGIN_PERMISSIONS = listOf("email", "public_profile")

        const val RESULT_SIGN_RESULT_DATA = "RESULT_SIGN_RESULT_DATA"

        const val RC_GOOGLE_SIGN_IN = 1

        fun launchIntent(context: Context): Intent {
            return Intent(context, SignInActivity::class.java)
        }
    }
}
