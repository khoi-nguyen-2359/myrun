package akio.apps.myrun.feature.profile

import akio.apps.common.feature.picker.PickPictureDelegate
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
    private val pickPictureButton: View by lazy { findViewById(R.id.btGallery) }

    // TODO: refactor to composable UI
    private lateinit var uploadUserAvatarImageUsecase: UploadUserAvatarImageUsecase

    private val takePictureDelegate = TakePictureDelegate(this, ::presentPictureContent)
    private val pickPictureDelegate = PickPictureDelegate(this, ::presentPictureContent)

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

        pickPictureButton.setOnClickListener {
            pickPictureDelegate.execute()
        }
    }

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
        fun launchIntent(context: Context): Intent =
            Intent(context, UploadAvatarActivity::class.java)
    }
}
