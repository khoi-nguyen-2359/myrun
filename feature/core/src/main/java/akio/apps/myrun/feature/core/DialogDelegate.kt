package akio.apps.myrun.feature.core

import akio.apps.myrun.feature.core.ktx.collectRepeatOnStarted
import akio.apps.myrun.feature.core.launchcatching.LaunchCatchingDelegate
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.LifecycleOwner

class DialogDelegate(private val context: Context) {

    private var progressDialog: Dialog? = null
    private var errorDialog: Dialog? = null

    fun toggleProgressDialog(visible: Boolean) {
        if (visible) {
            showProgressDialog()
        } else {
            dismissProgressDialog()
        }
    }

    fun showProgressDialog() {
        if (progressDialog == null || progressDialog?.isShowing == false) {
            progressDialog = showTransparentProgressDialog(context)
        }
    }

    fun dismissProgressDialog() {
        if (progressDialog?.isShowing == true) {
            progressDialog?.dismiss()
        }
        progressDialog = null
    }

    /**
     * Show error message if the input error is not null.
     */
    fun showAlertIfErrorExists(throwable: Throwable?) {
        throwable?.message?.let(::showErrorDialog)
    }

    fun collectLaunchCatchingError(
        lifecycleOwner: LifecycleOwner,
        launchCatchingDelegate: LaunchCatchingDelegate,
    ) = lifecycleOwner.collectRepeatOnStarted(launchCatchingDelegate.launchCatchingError) {
        toggleErrorDialog(it) {
            launchCatchingDelegate.setLaunchCatchingError(null)
        }
    }

    private fun toggleErrorDialog(error: Throwable?, onAcknowledgeError: () -> Unit) {
        if (error == null) {
            dismissErrorDialog()
        } else {
            showErrorDialogIfNotShowing(error.message)?.setOnDismissListener {
                onAcknowledgeError()
            }
        }
    }

    private fun dismissErrorDialog() {
        if (errorDialog?.isShowing == true) {
            errorDialog?.dismiss()
        }
        errorDialog = null
    }

    private fun showErrorDialogIfNotShowing(errorMessage: String?) =
        if (errorDialog == null || errorDialog?.isShowing == false) {
            showErrorDialog(context, errorMessage)
        } else {
            null
        }

    fun showErrorDialog(errorMessage: String?): Dialog {
        val dialogMessage = errorMessage
            ?: context.getString(R.string.dialog_delegate_unknown_error)
        return showErrorDialog(context, dialogMessage)
    }

    private fun showErrorDialog(
        context: Context,
        message: String?,
        onClose: DialogInterface.OnClickListener? = null,
    ): Dialog {
        val noneNullMessage = message ?: context.getString(R.string.dialog_delegate_unknown_error)
        val dialog = AlertDialog.Builder(context)
            .setMessage(noneNullMessage)
            .setPositiveButton(R.string.action_close) { dialog, which ->
                onClose?.onClick(dialog, which)
                dialog.dismiss()
            }
            .create()
        errorDialog = dialog
        dialog.show()
        return dialog
    }

    private fun showTransparentProgressDialog(context: Context): Dialog {
        val dialog = Dialog(context, R.style.TransparentDialog)
        dialog.setContentView(R.layout.dialog_transparent_progress)
        dialog.setCancelable(false)
        dialog.setCanceledOnTouchOutside(false)
        dialog.show()
        return dialog
    }
}
