package akio.apps.myrun.feature.profile

import akio.apps.myrun.base.di.NamedIoDispatcher
import akio.apps.myrun.domain.user.GetUserProfileUsecase
import akio.apps.myrun.domain.user.UploadUserAvatarImageUsecase
import akio.apps.myrun.feature.core.DialogDelegate
import akio.apps.myrun.feature.core.picker.PickPictureDelegate
import akio.apps.myrun.feature.core.picker.TakePictureDelegate
import akio.apps.myrun.feature.profile.di.DaggerUploadAvatarFeatureComponent
import akio.apps.myrun.feature.profile.ui.CropImageView
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.getSystemService
import androidx.core.view.get
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import coil.Coil
import coil.ImageLoader
import coil.request.Disposable
import coil.request.ImageRequest
import coil.size.Size
import com.google.android.material.appbar.MaterialToolbar
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

// TODO: convert to composable UI + MVVM
internal class UploadAvatarActivity : AppCompatActivity(R.layout.activity_upload_avatar) {

    private var initialImageRequestDisposable: Disposable? = null

    private val dialogDelegate by lazy { DialogDelegate(this) }
    private val cropImageView: CropImageView by lazy { findViewById(R.id.cropImageView) }
    private val topAppBar: MaterialToolbar by lazy { findViewById(R.id.topAppBar) }
    private val takePictureButton: View by lazy { findViewById(R.id.btCamera) }
    private val pickPictureButton: View by lazy { findViewById(R.id.btGallery) }
    private val rotateButton: View by lazy { findViewById(R.id.rotateButton) }

    @Inject
    lateinit var uploadUserAvatarImageUsecase: UploadUserAvatarImageUsecase

    @Inject
    lateinit var getUserProfileUsecase: GetUserProfileUsecase

    private val takePictureDelegate = TakePictureDelegate(this, ::presentPictureContent)
    private val pickPictureDelegate = PickPictureDelegate(this, ::presentPictureContent)

    private val imageLoader: ImageLoader by lazy { Coil.imageLoader(this) }

    @Inject
    @NamedIoDispatcher
    lateinit var ioDispatcher: CoroutineDispatcher

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val domainComponent = DaggerUploadAvatarFeatureComponent.factory().create(application)
        domainComponent.inject(this)
//        uploadUserAvatarImageUsecase = domainComponent.uploadUserAvatarImageUsecase()
//        getUserProfileUsecase = domainComponent.getUserProfileUsecase()

        loadInitialUserProfilePicture()

        rotateButton.setOnClickListener {
            if (cropImageView.rotation % 90 == 0f) {
                cropImageView.animate().rotationBy(-90f)
            }
        }

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

    private fun loadInitialUserProfilePicture() = lifecycleScope.launch {
        val userProfilePictureUrl = withContext(ioDispatcher) {
            getUserProfileUsecase.getUserProfileResource().data?.photo
        }
            ?: return@launch

        val initialImageRequest = ImageRequest.Builder(this@UploadAvatarActivity)
            .data(userProfilePictureUrl)
            .lifecycle(lifecycle)
            .size(Size.ORIGINAL)
            .allowConversionToBitmap(true)
            .allowHardware(false) // don't use hardware bitmap because we need to edit it later.
            .target { result ->
                val bitmapDrawable = result as? BitmapDrawable ?: return@target
                setImageBitmap(bitmapDrawable.bitmap)
            }
            .build()
        initialImageRequestDisposable = imageLoader.enqueue(initialImageRequest)
    }

    private fun presentPictureContent(photoContentUri: Uri) {
        // to avoid the initial picture override user selected picture.
        initialImageRequestDisposable?.dispose()
        contentResolver.openInputStream(photoContentUri)?.use {
            setImageBitmap(BitmapFactory.decodeStream(it))
        }
    }

    private fun setImageBitmap(imageBitmap: Bitmap) {
        cropImageView.setImageBitmap(imageBitmap)
        // enable edit controls
        topAppBar.menu[0].isEnabled = true
        rotateButton.isVisible = true
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

    private suspend fun uploadUserAvatarFile(bitmapTmpFile: File) = withContext(ioDispatcher) {
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
