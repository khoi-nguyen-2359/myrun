package akio.apps.myrun.feature.profile

import akio.apps.common.feature.picker.TakePictureDelegate
import akio.apps.myrun.domain.user.UploadUserAvatarImageUsecase
import akio.apps.myrun.feature.base.DialogDelegate
import akio.apps.myrun.feature.profile.ui.CropImageView
import akio.apps.myrun.wiring.domain.DaggerDomainComponent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.getSystemService
import androidx.lifecycle.lifecycleScope
import com.google.android.material.appbar.MaterialToolbar
import java.io.File
import java.io.FileOutputStream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class UploadAvatarActivity : AppCompatActivity(R.layout.activity_upload_avatar) {

    private val dialogDelegate by lazy { DialogDelegate(this) }
    private val cropImageView: CropImageView by lazy { findViewById(R.id.cropImageView) }
    private val topAppBar: MaterialToolbar by lazy { findViewById(R.id.topAppBar) }
    private val takePictureButton: View by lazy { findViewById(R.id.btCamera) }

    // TODO: refactor to composable UI
    lateinit var uploadUserAvatarImageUsecase: UploadUserAvatarImageUsecase

//    private val photoSelectionDelegate = PhotoSelectionDelegate(
//        activity = this,
//        fragment = null,
//        requestCodes = PhotoSelectionDelegate.RequestCodes(
//            RC_TAKE_PHOTO_PERMISSIONS,
//            RC_PICK_PHOTO_PERMISSIONS,
//            RC_TAKE_PHOTO,
//            RC_PICK_PHOTO
//        ),
//        eventListener = this
//    )

    private val takePictureDelegate = TakePictureDelegate(this, ::presentPictureContent)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        uploadUserAvatarImageUsecase =
            DaggerDomainComponent.create().uploadUserAvatarImageUsecase()

        topAppBar.setNavigationOnClickListener {
            finish()
        }

        topAppBar.setOnMenuItemClickListener {
            cropAndUploadAvatar()
            true
        }

        takePictureButton.setOnClickListener {
            takePictureDelegate.execute()
        }
    }

//    @Suppress("DEPRECATION")
//    override fun onRequestPermissionsResult(
//        requestCode: Int,
//        permissions: Array<out String>,
//        grantResults: IntArray,
//    ) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
//        photoSelectionDelegate.onRequestPermissionsResult(requestCode)
//    }
//
//    @Suppress("DEPRECATION")
//    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        super.onActivityResult(requestCode, resultCode, data)
//        photoSelectionDelegate.onActivityResult(requestCode, resultCode, data)
//    }

    private fun presentPictureContent(photoContentUri: Uri) {
        contentResolver.openInputStream(photoContentUri)?.use {
            val imageBitmap = BitmapFactory.decodeStream(it)
            cropImageView.setImageBitmap(imageBitmap)
        }
    }

    @Suppress("DEPRECATION") // activeNetworkInfo.isConnectedOrConnecting
    private fun cropAndUploadAvatar() {
        val connMan = getSystemService<ConnectivityManager>()
        if (connMan?.activeNetworkInfo?.isConnectedOrConnecting != true) {
            dialogDelegate.showErrorAlert(
                getString(R.string.upload_avatar_connection_unavailable_error)
            )
            return
        }

        lifecycleScope.launch {
            dialogDelegate.showProgressDialog()
            try {
                val croppedBitmap = cropImageView.crop()
                if (croppedBitmap != null) {
                    val bitmapTmpFile = createCroppedBitmapTempFile(croppedBitmap)
                    uploadUserAvatarFile(bitmapTmpFile)
                    bitmapTmpFile.delete()
                    Toast.makeText(
                        this@UploadAvatarActivity,
                        R.string.upload_avatar_sucess_message,
                        Toast.LENGTH_SHORT
                    ).show()
                    finish()
                }
            } catch (ex: Exception) {
                dialogDelegate.showExceptionAlert(ex)
            }
            dialogDelegate.dismissProgressDialog()
        }
    }

    private suspend fun uploadUserAvatarFile(bitmapTmpFile: File) {
        val fileUri = Uri.fromFile(bitmapTmpFile)
        uploadUserAvatarImageUsecase.uploadUserAvatarImage(fileUri.toString())
    }

    private suspend fun createCroppedBitmapTempFile(croppedBitmap: Bitmap) =
        withContext(Dispatchers.IO) {
            val tempFile = File.createTempFile("cropped_avatar_", ".jpg")
            val output = FileOutputStream(tempFile)
            croppedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, output)
            output.flush()
            output.close()

            tempFile
        }

    companion object {
        private const val RC_TAKE_PHOTO_PERMISSIONS = 1
        private const val RC_PICK_PHOTO_PERMISSIONS = 2
        private const val RC_TAKE_PHOTO = 3
        private const val RC_PICK_PHOTO = 4

        fun launchIntent(context: Context): Intent =
            Intent(context, UploadAvatarActivity::class.java)
    }
}
