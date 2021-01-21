package akio.apps._base.utils

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment

object PermissionUtils {
    /**
     * @param fragment Use fragment if you are requesting permissions from inside fragment
     * @param onPermissionSettingsDisabled Callback for permissions that needed to be enabled in system settings page
     */
    fun requestPermissions(
        activity: Activity,
        permissions: Array<String>,
        rcPermissions: Int,
        onPermissionSettingsDisabled: (() -> Unit)?,
        fragment: Fragment? = null
    ) {
        if (permissions.any {
                ContextCompat.checkSelfPermission(activity, it) != PackageManager.PERMISSION_GRANTED
                    && !ActivityCompat.shouldShowRequestPermissionRationale(activity, it)
            }) {
            onPermissionSettingsDisabled?.invoke()
        } else {
            if (fragment != null) {
                fragment.requestPermissions(permissions, rcPermissions)
            } else {
                ActivityCompat.requestPermissions(activity, permissions, rcPermissions)
            }
        }
    }

    fun arePermissionsGranted(context: Context, permissions: Array<String>): Boolean {
        return permissions.all {
            ContextCompat.checkSelfPermission(
                context,
                it
            ) == PackageManager.PERMISSION_GRANTED
        }
    }

}
