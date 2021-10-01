package akio.apps.myrun.feature.base.picker

import akio.apps.myrun.feature.base.permissions.AppPermissions
import android.net.Uri
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity

class PickPictureDelegate(
    activity: AppCompatActivity,
    private val pictureReadyAction: (Uri) -> Unit,
) : PictureSelectionDelegate<String>(
    activity,
    AppPermissions.pickPhotoPermissions
) {

    override val actionLauncher: ActivityResultLauncher<String> =
        activity.registerForActivityResult(ActivityResultContracts.GetContent()) { contentUri ->
            contentUri?.let(pictureReadyAction)
        }

    override fun launchAction() {
        actionLauncher.launch("image/*")
    }
}
