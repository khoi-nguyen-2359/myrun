package akio.apps.myrun._base.utils

import akio.apps._base.utils.PermissionUtils
import akio.apps.myrun.R
import akio.apps.myrun._base.permissions.AppPermissions
import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment


class PhotoSelectionDelegate(
	private val activity: Activity,
	private val fragment: Fragment?,
	private val requestCodes: RequestCodes,
	private var eventListener: EventListener? = null
) {

    private var capturedPhotoContentUri: Uri? = null

    fun showPhotoSelectionDialog(title: String) {
        val options = activity.resources.getStringArray(R.array.photo_selection_options)
        AlertDialog.Builder(activity)
            .setTitle(title)
            .setItems(options) { dialog, which ->
				when (which) {
					0 -> requestPermissions(AppPermissions.takePhotoPermissions, requestCodes.rcTakePhotoPermissions)
					1 -> requestPermissions(AppPermissions.pickPhotoPermissions, requestCodes.rcPickPhotoPermissions)
				}
			}
			.create()
    }

    private fun requestPermissions(permissions: Array<String>, requestCode: Int) {
        if (fragment != null) {
            fragment.requestPermissions(permissions, requestCode)
        } else {
            ActivityCompat.requestPermissions(activity, permissions, requestCode)
        }
    }

    private fun openGallery() {
        val galleryIntent = Intent(Intent.ACTION_OPEN_DOCUMENT)
            .apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "image/*"
            }
        startActivityForResult(galleryIntent, requestCodes.rcPickPhoto)
    }

    private fun startActivityForResult(intent: Intent, rc: Int) {
        if (fragment != null) {
            fragment.startActivityForResult(intent, rc)
        } else {
            activity.startActivityForResult(intent, rc)
        }
    }

    private fun launchCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        intent.resolveActivity(activity.packageManager)?.also {
            val values = ContentValues(1)
            values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpg")
            capturedPhotoContentUri = activity.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
            intent.putExtra(MediaStore.EXTRA_OUTPUT, capturedPhotoContentUri)
            startActivityForResult(intent, requestCodes.rcTakePhoto)
        }
    }

    fun onRequestPermissionsResult(requestCode: Int) {
        when (requestCode) {
			requestCodes.rcTakePhotoPermissions -> {
				if (PermissionUtils.arePermissionsGranted(activity, AppPermissions.takePhotoPermissions)) {
					launchCamera()
				}
			}

			requestCodes.rcPickPhotoPermissions -> {
				if (PermissionUtils.arePermissionsGranted(activity, AppPermissions.pickPhotoPermissions)) {
					openGallery()
				}
			}
        }
    }

    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        handleCapturedPhoto(requestCode, resultCode, data)
        handlePickedPhoto(requestCode, resultCode, data)
    }

    private fun handlePickedPhoto(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode != requestCodes.rcPickPhoto || resultCode != Activity.RESULT_OK || data == null)
            return

        data.data?.let {
			eventListener?.onPhotoSelectionReady(it)
		}
    }

    private fun handleCapturedPhoto(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode != requestCodes.rcTakePhoto || resultCode != Activity.RESULT_OK || data == null)
            return

        capturedPhotoContentUri
            ?.let {
                eventListener?.onPhotoSelectionReady(it)
                capturedPhotoContentUri = null
            }
    }

    class RequestCodes(
		val rcTakePhotoPermissions: Int,
		val rcPickPhotoPermissions: Int,
		val rcTakePhoto: Int,
		val rcPickPhoto: Int
	)

    interface EventListener {
        fun onPhotoSelectionReady(photoContentUri: Uri)
    }
}