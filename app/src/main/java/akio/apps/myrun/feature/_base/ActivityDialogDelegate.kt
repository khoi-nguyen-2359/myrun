package akio.apps.myrun.feature._base

import akio.apps.myrun.R
import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import androidx.appcompat.app.AlertDialog

class ActivityDialogDelegate(
    private val activity: Activity
) {

    private var progressDialog: Dialog? = null

    val a by lazy { this }


    fun toggleProgressDialog(visible: Boolean) {
        if (visible) {
            showProgressDialog()
        } else {
            hideProgressDialog()
        }
    }

    fun showProgressDialog() {
        if (progressDialog == null || progressDialog?.isShowing == false) {
            progressDialog = showTransparentProgressDialog(activity)
        }
    }

    fun hideProgressDialog() {
        if (activity.isFinishing || activity.isDestroyed)
            return

        progressDialog?.dismiss()
    }

    fun showExceptionAlert(throwable: Throwable) {
        showErrorAlert(throwable.message)
    }

    fun showErrorAlert(errorMessage: String?) {
        val dialogMessage = errorMessage ?: activity.getString(R.string.error_unknown)
        showErrorAlert(activity, dialogMessage)
    }

    private fun showErrorAlert(context: Context, message: String?, onClose: DialogInterface.OnClickListener? = null): Dialog {
        val noneNullMessage = message ?: context.getString(R.string.error_unknown)
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