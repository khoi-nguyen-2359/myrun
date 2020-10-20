package akio.apps.myrun._base.permissions

import akio.apps._base.utils.PermissionUtils
import akio.apps.myrun.R
import android.app.Activity
import android.content.Context
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment

object RequiredPermissionsDelegate {

    fun requestPermissions(permissions: Array<String>, rcPermissions: Int, activity: Activity, fragment: Fragment? = null) {
        if (fragment != null) {
            fragment.requestPermissions(permissions, rcPermissions)
        } else {
            ActivityCompat.requestPermissions(activity, permissions, rcPermissions)
        }
    }

    fun verifyPermissionsResult(context: Context, permissions: Array<String>, onPermissionsGranted: () -> Any, onPermissionsDenied: () -> Any) {
        if (PermissionUtils.arePermissionsGranted(context, permissions)) {
            onPermissionsGranted()
            return
        }

        if (!PermissionUtils.arePermissionsGranted(context, permissions)) {
            AlertDialog.Builder(context)
                .setMessage(R.string.error_required_permissions_missing)
                .setPositiveButton(R.string.action_close) { _, _->
                    onPermissionsDenied()
                }
                .setCancelable(false)
                .show()
        }
    }
}