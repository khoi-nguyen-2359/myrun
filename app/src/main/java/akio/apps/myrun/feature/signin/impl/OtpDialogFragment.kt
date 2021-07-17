package akio.apps.myrun.feature.signin.impl

import akio.apps._base.ui.SoftKeyboardHelper
import akio.apps.myrun.OTP_VERIFY_TIMEOUT
import akio.apps.myrun.R
import akio.apps.myrun._base.utils.DialogDelegate
import akio.apps.myrun.databinding.DialogOtpBinding
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import com.google.firebase.FirebaseException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Timer
import java.util.concurrent.TimeUnit
import kotlin.concurrent.fixedRateTimer

@Suppress("DEPRECATION")
class OtpDialogFragment : DialogFragment() {

    private var resendTimer: Timer? = null

    private lateinit var resendToken: PhoneAuthProvider.ForceResendingToken
    private lateinit var verificationId: String

    var eventListener: EventListener? = null

    private val dialogDelegate by lazy { DialogDelegate(requireActivity()) }

    private var _viewBinding: DialogOtpBinding? = null
    private val viewBinding get() = _viewBinding!!

    private val phone by lazy {
        arguments?.getString(ARG_PHONE) ?: throw Exception("Phone is missing")
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _viewBinding = DialogOtpBinding.inflate(inflater, container, false)
        return viewBinding.root
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = Dialog(requireContext(), R.style.TransparentDialog)
        dialog.setOnShowListener { verifyPhoneNumber() }
        return dialog
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews()
    }

    private fun initViews() = viewBinding.apply {
        confirmButton.setOnClickListener { onClickConfirm() }
        resendButton.setOnClickListener { verifyPhoneNumber() }
        otpEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                onClickConfirm()
                return@setOnEditorActionListener true
            }

            false
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        resendTimer?.cancel()
        eventListener = null
        _viewBinding = null
    }

    private fun onClickConfirm() {
        val otp = viewBinding.otpEditText.text.toString()
        if (otp.isEmpty()) {
            dialogDelegate.showErrorAlert(getString(R.string.otp_invalid_code_error))
            return
        }

        eventListener?.onConfirmOtp(PhoneAuthProvider.getCredential(verificationId, otp))
    }

    private fun verifyPhoneNumber() {
        dialogDelegate.showProgressDialog()
        PhoneAuthProvider.getInstance()
            .verifyPhoneNumber(
                phone,
                OTP_VERIFY_TIMEOUT,
                TimeUnit.SECONDS,
                requireActivity(),
                object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                    override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                        dialogDelegate.dismissProgressDialog()
                        eventListener?.onConfirmOtp(credential)
                    }

                    override fun onVerificationFailed(p0: FirebaseException) {
                        dialogDelegate.dismissProgressDialog()
                        dialogDelegate.showErrorAlert(p0.message)
                            .setOnDismissListener { dismiss() }
                    }

                    override fun onCodeSent(
                        verificationId: String,
                        resendToken: PhoneAuthProvider.ForceResendingToken
                    ) {
                        this@OtpDialogFragment.verificationId = verificationId
                        this@OtpDialogFragment.resendToken = resendToken
                        dialogDelegate.dismissProgressDialog()
                        viewBinding.otpEditText.visibility = View.VISIBLE
                        lifecycleScope.launch {
                            delay(100)
                            viewBinding.otpEditText.requestFocus()
                            SoftKeyboardHelper.showKeyboard(viewBinding.otpEditText)
                        }
                        startResendCountdown()
                    }
                }
            )
    }

    private fun startResendCountdown() {
        viewBinding.resendButton.isEnabled = false
        viewBinding.resendButton.tag = OTP_VERIFY_TIMEOUT
        resendTimer = fixedRateTimer("resend_timer", true, 0, 1000) {
            viewBinding.resendButton.post {
                if (!isDetached) {
                    val count = viewBinding.resendButton.tag as? Long
                    if (count == 0L) {
                        resendTimer?.cancel()
                        viewBinding.resendButton.isEnabled = true
                        viewBinding.resendButton.setText(R.string.otp_resend_button)
                    } else if (count != null) {
                        viewBinding.resendButton.text =
                            getString(R.string.otp_resend_template, count)
                        viewBinding.resendButton.tag = count - 1
                    }
                }
            }
        }
    }

    interface EventListener {
        fun onConfirmOtp(phoneAuthCredential: PhoneAuthCredential)
    }

    companion object {
        const val ARG_PHONE = "ARG_PHONE"

        fun instantiate(phone: String): OtpDialogFragment {
            val f = OtpDialogFragment()
            f.arguments = Bundle().apply {
                putString(ARG_PHONE, phone)
            }

            return f
        }
    }
}
