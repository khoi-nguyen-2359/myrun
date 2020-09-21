package akio.apps.myrun.feature._base.permissions

import akio.apps._base.utils.PermissionUtils
import akio.apps.myrun.R
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment

class CheckRequiredPermissionsDelegate(
    private val activity: Activity,
    private val rcPermissions: Int,
    private val permissions: Array<String>,
    private val onPermissionsGranted: () -> Any,
    private val fragment: Fragment? = null
) {

    fun requestPermissions() {
        if (PermissionUtils.arePermissionsGranted(activity, permissions)) {
            onPermissionsGranted()
            return
        }

        if (fragment != null) {
            fragment.requestPermissions(permissions, rcPermissions)
        } else {
            ActivityCompat.requestPermissions(activity, permissions, rcPermissions)
        }
    }

    fun verifyPermissionsResult() {
        if (PermissionUtils.arePermissionsGranted(activity, permissions)) {
            onPermissionsGranted()
            return
        }

        if (!PermissionUtils.arePermissionsGranted(activity, permissions)) {
            AlertDialog.Builder(activity)
                .setMessage(R.string.error_required_permissions_missing)
                .setPositiveButton(R.string.action_close) { _, _->
                    activity.finish()
                }
                .setCancelable(false)
                .show()
        }
    }
}