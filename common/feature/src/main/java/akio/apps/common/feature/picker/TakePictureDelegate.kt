package akio.apps.common.feature.picker

import akio.apps.common.feature.R
import akio.apps.common.feature.permissions.AppPermissions
import android.content.ContentValues
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.net.Uri
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class TakePictureDelegate(
    private val activity: AppCompatActivity,
    private val pictureReadyAction: (Uri) -> Unit,
) {

    private var capturedContentUri: Uri? = null

    private val requestPermissionsLauncher =
        activity.registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { isGrantedMap: Map<String, Boolean> ->
            if (isGrantedMap.all { it.value }) {
                launchCamera()
            } else {
                Toast.makeText(
                    activity,
                    R.string.take_picture_permissions_missing_error,
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

    private val takePictureLauncher =
        activity.registerForActivityResult(ActivityResultContracts.TakePicture()) { result ->
            if (result) {
                capturedContentUri?.let(pictureReadyAction)
                capturedContentUri = null
            }
        }

    fun execute() {
        val isGranted = !AppPermissions.takePhotoPermissions.any { permission ->
            ContextCompat.checkSelfPermission(activity, permission) != PERMISSION_GRANTED
        }
        if (isGranted) {
            launchCamera()
            return
        }

        val shouldShowRationale = AppPermissions.takePhotoPermissions.any { permission ->
            ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)
        }

        if (shouldShowRationale) {
            showPermissionRationale(activity)
            return
        }

        requestPermissions()
    }

    private fun requestPermissions() {
        requestPermissionsLauncher.launch(AppPermissions.takePhotoPermissions)
    }

    private fun showPermissionRationale(activity: AppCompatActivity) {
        AlertDialog.Builder(activity)
            .setMessage(R.string.take_picture_permissions_rationale_message)
            .setNegativeButton(R.string.action_no) { dialog, _ -> dialog.dismiss() }
            .setPositiveButton(R.string.action_agree) { _, _ -> requestPermissions() }
            .show()
    }

    private fun launchCamera() {
        val values = ContentValues(1)
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
        capturedContentUri = activity.contentResolver.insert(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            values
        )
        takePictureLauncher.launch(capturedContentUri)
    }
}
