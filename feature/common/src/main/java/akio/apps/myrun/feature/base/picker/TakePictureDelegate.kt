package akio.apps.myrun.feature.base.picker

import akio.apps.myrun.feature.base.permissions.AppPermissions
import android.content.ContentValues
import android.net.Uri
import android.provider.MediaStore
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity

class TakePictureDelegate(
    activity: AppCompatActivity,
    private val pictureReadyAction: (Uri) -> Unit,
) : PictureSelectionDelegate<Uri>(
    activity,
    AppPermissions.takePhotoPermissions
) {

    private var capturedContentUri: Uri? = null

    override val actionLauncher: ActivityResultLauncher<Uri> =
        activity.registerForActivityResult(ActivityResultContracts.TakePicture()) { result ->
            if (result) {
                capturedContentUri?.let(pictureReadyAction)
            }
        }

    override fun launchAction() {
        val values = ContentValues(1)
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
        capturedContentUri = activity.contentResolver.insert(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            values
        )
        actionLauncher.launch(capturedContentUri)
    }
}
