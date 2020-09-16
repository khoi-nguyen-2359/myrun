package akio.apps.myrun.feature._base

import akio.apps._base.PermissionUtils
import akio.apps.myrun.R
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.appcompat.app.AlertDialog
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

        PermissionUtils.requestPermissions(activity, permissions, rcPermissions, {
            AlertDialog.Builder(activity)
                .setMessage(R.string.error_permission_settings_disabled)
                .setPositiveButton(R.string.action_open_settings) { _, _ ->
                    Intent().apply {
                        action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS;
                        data = Uri.parse("package:${activity.packageName}");
                        activity.startActivity(this)
                    }
                }
                .setNegativeButton(R.string.action_cancel) { _, _ ->
                    activity.finish()
                }
                .setCancelable(false)
                .create()
                .show()
        }, fragment)
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