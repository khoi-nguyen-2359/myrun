package akio.apps.myrun.feature.core

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import androidx.appcompat.app.AlertDialog

class DialogDelegate(private val context: Context) {

    private var progressDialog: Dialog? = null

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
        if (progressDialog?.isShowing != true) {
            return
        }

        progressDialog?.dismiss()
    }

    fun showExceptionAlert(throwable: Throwable) {
        showErrorAlert(throwable.message)
    }

    fun showErrorAlert(errorMessage: String?): Dialog {
        val dialogMessage = errorMessage
            ?: context.getString(R.string.dialog_delegate_unknown_error)
        return showErrorAlert(context, dialogMessage)
    }

    private fun showErrorAlert(
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
