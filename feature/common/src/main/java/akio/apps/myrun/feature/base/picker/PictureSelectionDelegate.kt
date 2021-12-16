package akio.apps.myrun.feature.base.picker

import akio.apps.myrun.feature.base.R
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

abstract class PictureSelectionDelegate<Input>(
    protected val activity: AppCompatActivity,
    protected val permissions: Array<String>
) {
    private val requestPermissionsLauncher =
        activity.registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { isGrantedMap: Map<String, Boolean> ->
            if (isGrantedMap.all { it.value }) {
                launchAction()
            } else {
                Toast.makeText(
                    activity,
                    R.string.alert_permissions_missing_message,
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

    protected abstract val actionLauncher: ActivityResultLauncher<Input>

    fun execute() {
        val isGranted = !permissions.any { permission ->
            ContextCompat.checkSelfPermission(
                activity,
                permission
            ) != PackageManager.PERMISSION_GRANTED
        }
        if (isGranted) {
            launchAction()
            return
        }

        val shouldShowRationale = permissions.any { permission ->
            ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)
        }

        if (shouldShowRationale) {
            showPermissionRationale(activity)
            return
        }

        requestPermissions()
    }

    private fun requestPermissions() = requestPermissionsLauncher.launch(permissions)

    private fun showPermissionRationale(activity: AppCompatActivity) {
        AlertDialog.Builder(activity)
            .setMessage(R.string.alert_permissions_rationale_message)
            .setNegativeButton(R.string.action_no) { dialog, _ -> dialog.dismiss() }
            .setPositiveButton(R.string.action_agree) { _, _ -> requestPermissions() }
            .show()
    }

    protected abstract fun launchAction()
}
