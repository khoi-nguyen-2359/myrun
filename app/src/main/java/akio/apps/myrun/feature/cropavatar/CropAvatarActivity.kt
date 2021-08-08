package akio.apps.myrun.feature.cropavatar

import akio.apps.myrun.databinding.ActivityCropImageBinding
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import java.io.File
import java.io.FileOutputStream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CropAvatarActivity : AppCompatActivity() {

    private val imageContentUri by lazy { intent.getParcelableExtra<Uri>(EXT_FILE) as Uri }

    private val viewBinding by lazy { ActivityCropImageBinding.inflate(layoutInflater) }

    private val dialogDelegate by lazy { akio.apps.myrun.feature.base.DialogDelegate(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(viewBinding.root)

        contentResolver.openInputStream(imageContentUri)
            ?.use {
                val imageBitmap = BitmapFactory.decodeStream(it)
                viewBinding.cropImageView.setImageBitmap(imageBitmap)
            }
    }

    @Suppress("UNUSED_PARAMETER")
    fun onClickCrop(view: View) {
        viewBinding.cropImageView.crop()
            ?.let {
                lifecycleScope.launch {
                    dialogDelegate.showProgressDialog()

                    val tempFile = withContext(Dispatchers.IO) {
                        val tempFile = File.createTempFile("cropped_avatar_", ".jpg")
                        val output = FileOutputStream(tempFile)
                        it.compress(Bitmap.CompressFormat.JPEG, 100, output)
                        output.flush()
                        output.close()

                        tempFile
                    }

                    withContext(Dispatchers.Main) {
                        val data = Intent()
                        data.putExtra(RESULT_CROPPED_AVATAR_FILE, tempFile)
                        setResult(RESULT_OK, data)
                        finish()
                    }
                }
            }
    }

    companion object {
        const val EXT_FILE = "EXT_FILE"

        const val RESULT_CROPPED_AVATAR_FILE = "RESULT_CROPPED_AVATAR_FILE"

        fun launchIntent(context: Context, imageContentUri: Uri): Intent {
            val intent = Intent(context, CropAvatarActivity::class.java)
            intent.putExtra(EXT_FILE, imageContentUri)
            return intent
        }
    }
}
