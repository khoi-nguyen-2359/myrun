package akio.apps.myrun.base.firebase

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import kotlin.math.roundToInt

object BitmapUtils {
    private fun calculateInSampleSize(
        options: BitmapFactory.Options,
        reqWidth: Int,
        reqHeight: Int,
    ): Int {
        // Raw height and width of image
        val (height: Int, width: Int) = options.run { outHeight to outWidth }
        var inSampleSize = 1

        if (height > reqHeight || width > reqWidth) {

            val halfHeight: Int = height / 2
            val halfWidth: Int = width / 2

            while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
                inSampleSize *= 2
            }
        }

        return inSampleSize
    }

    fun decodeSampledFile(
        filePath: String,
        reqWidth: Int,
        reqHeight: Int,
    ): Bitmap = BitmapFactory.Options()
        .run {
            inJustDecodeBounds = true
            BitmapFactory.decodeFile(filePath, this)

            // Calculate inSampleSize
            inSampleSize = calculateInSampleSize(this, reqWidth, reqHeight)

            // Decode bitmap with inSampleSize set
            inJustDecodeBounds = false

            BitmapFactory.decodeFile(filePath, this)
        }

    fun scale(origin: Bitmap, maxWidthAndHeight: Int): Bitmap {
        val newWidth: Int
        val newHeight: Int

        if (origin.width >= origin.height) {
            val ratio: Float = origin.width.toFloat() / origin.height.toFloat()

            newWidth = maxWidthAndHeight
            // Calculate the new height for the scaled bitmap
            newHeight = (maxWidthAndHeight / ratio).roundToInt()
        } else {
            val ratio: Float = origin.height.toFloat() / origin.width.toFloat()

            // Calculate the new width for the scaled bitmap
            newWidth = (maxWidthAndHeight / ratio).roundToInt()
            newHeight = maxWidthAndHeight
        }

        return Bitmap.createScaledBitmap(origin, newWidth, newHeight, false)
    }
}
