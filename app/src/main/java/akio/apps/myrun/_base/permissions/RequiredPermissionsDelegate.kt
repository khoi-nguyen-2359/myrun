package akio.apps.myrun._base.permissions

import akio.apps._base.utils.PermissionUtils
import akio.apps.myrun.R
import android.app.Activity
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment

class RequiredPermissionsDelegate(
    private val activity: Activity,
    private val fragment: Fragment? = null
) {

    fun requestPermissions(permissions: Array<String>, rcPermissions: Int) {
//        if (PermissionUtils.arePermissionsGranted(activity, permissions)) {
//            onPermissionsGranted()
//            return
//        }

        if (fragment != null) {
            fragment.requestPermissions(permissions, rcPermissions)
        } else {
            ActivityCompat.requestPermissions(activity, permissions, rcPermissions)
        }
    }

    fun verifyPermissionsResult(permissions: Array<String>, onPermissionsGranted: () -> Any) {
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