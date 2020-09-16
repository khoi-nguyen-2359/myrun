package akio.apps.myrun.feature.common

import akio.apps.common.PermissionUtils
import akio.apps.myrun.R
import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment

object AppPermissions {
    private val locationPermissions = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)

    fun requestLocationPermissions(activity: Activity, fragment: Fragment?, requestCode: Int, onPermissionsDenied: (() -> Unit)? = null) {
        requestAppPermissions(activity, fragment, locationPermissions, requestCode, onPermissionsDenied)
    }

    fun verifyPermissions(context: Context, permissions: Array<String>, onPermissionsDenied: (() -> Unit)) {
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

    private fun requestAppPermissions(
        activity: Activity,
        fragment: Fragment?,
        permissions: Array<String>,
        requestCode: Int,
        onPermissionsDenied: (() -> Unit)?
    ) {
        PermissionUtils.requestPermissions(activity, fragment, permissions, requestCode) {
            showMissingPermissionsDialog(
                activity,
                onPermissionsDenied
            )
        }
    }

    private fun showMissingPermissionsDialog(activity: Activity, onFailureResult: (() -> Unit)? = null) {
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
                onFailureResult?.invoke()
            }
            .setCancelable(false)
            .create()
            .show()
    }
}