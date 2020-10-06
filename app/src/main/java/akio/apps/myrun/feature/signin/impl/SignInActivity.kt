package akio.apps.myrun.feature.signin.impl

import akio.apps._base.lifecycle.observe
import akio.apps._base.lifecycle.observeEvent
import akio.apps.myrun._di.createViewModelInjectionDelegate
import akio.apps.myrun.databinding.ActivitySignInBinding
import akio.apps.myrun.feature._base.utils.DialogDelegate
import akio.apps.myrun.feature.signin.SignInViewModel
import akio.apps.myrun.feature.signin.view.PhoneBox
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.firebase.auth.PhoneAuthCredential


class SignInActivity : AppCompatActivity(), OtpDialogFragment.EventListener {

    private val viewModelInjectionDelegate by lazy { createViewModelInjectionDelegate() }
    private val signInVM: SignInViewModel by lazy { viewModelInjectionDelegate.getViewModel() }

    private val viewBinding: ActivitySignInBinding by lazy { ActivitySignInBinding.inflate(layoutInflater) }

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
                LoginManager.getInstance().logInWithReadPermissions(this@SignInActivity, FB_LOGIN_PERMISSIONS)
            }

            phoneBox.eventListener = object : PhoneBox.EventListener {
                override fun onPhoneBoxValueChanged(value: String?) {
                    loginButton.isEnabled = value != null
                }
            }

            loginButton.setOnClickListener { openOtp() }
        }
    }

    override fun onAttachFragment(fragment: Fragment) {
        super.onAttachFragment(fragment)

        when (fragment) {
            is OtpDialogFragment -> fragment.eventListener = this
        }
    }

    private fun openOtp() {
        val phone = viewBinding.phoneBox.getFullNumber()
            ?: return
        OtpDialogFragment.instantiate(phone)
            .show(supportFragmentManager, null)
    }

    private fun onSignInSuccess(signInSuccessResult: SignInSuccessResult) {
        (supportFragmentManager.findFragmentByTag(FRAGMENT_TAG_OTP_DIALOG)
                as? OtpDialogFragment)?.dismiss()

        val resultIntent = Intent()
        resultIntent.putExtra(RESULT_SIGN_RESULT_DATA, signInSuccessResult)
        setResult(Activity.RESULT_OK, resultIntent)
        finish()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        facebookCallbackManager.onActivityResult(requestCode, resultCode, data)
        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onDestroy() {
        super.onDestroy()

        LoginManager.getInstance().unregisterCallback(facebookCallbackManager)
    }

    override fun onConfirmOtp(phoneAuthCredential: PhoneAuthCredential) {
        signInVM.signInWithFirebasePhoneCredential(phoneAuthCredential)
    }

    companion object {
        const val FRAGMENT_TAG_OTP_DIALOG = "FRAGMENT_TAG_OTP_DIALOG"

        val FB_LOGIN_PERMISSIONS = listOf("email", "public_profile")

        const val RESULT_SIGN_RESULT_DATA = "RESULT_SIGN_RESULT_DATA"

        fun launchIntent(context: Context): Intent {
            return Intent(context, SignInActivity::class.java)
        }
    }
}
