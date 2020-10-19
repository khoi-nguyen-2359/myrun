package akio.apps._base.utils

import akio.apps._base.ui.BitmapUtils
import android.graphics.Bitmap
import android.net.Uri
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.tasks.await
import java.io.ByteArrayOutputStream
import java.io.File

object FirebaseStorageUtils {

    suspend fun uploadBitmap(storageRef: StorageReference, storeName: String, bitmap: Bitmap, scaledSize: Int): Uri {
        val photoRef = storageRef.child(storeName)

        val uploadBitmap = if (scaledSize != 1)
            BitmapUtils.scale(bitmap, scaledSize)
        else
            bitmap

        val uploadBaos = ByteArrayOutputStream()
        uploadBitmap.compress(Bitmap.CompressFormat.JPEG, 100, uploadBaos)
        val uploadData = uploadBaos.toByteArray()
        photoRef.putBytes(uploadData).await()
        uploadBaos.close()
        uploadBitmap.recycle()
        return photoRef.downloadUrl.await()
    }

    suspend fun uploadLocalBitmap(storage: StorageReference, storeName: String, filePath: String, scaledSize: Int): Uri? {
        val imageBitmap = BitmapUtils.decodeSampledFile(filePath, scaledSize, scaledSize)
        val downloadUrl = uploadBitmap(storage, storeName, imageBitmap, 1)
        imageBitmap.recycle()

        return downloadUrl
    }
}