package akio.apps._base.ui

import android.graphics.Bitmap
import android.graphics.BitmapFactory

object BitmapUtils {
    fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
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

    fun decodeSampledByteArray(
        byteArray: ByteArray,
        reqWidth: Int,
        reqHeight: Int
    ): Bitmap {
        return BitmapFactory.Options().run {
            inJustDecodeBounds = true
            BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size, this)

            // Calculate inSampleSize
            inSampleSize = calculateInSampleSize(this, reqWidth, reqHeight)

            // Decode bitmap with inSampleSize set
            inJustDecodeBounds = false

            BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size, this)
        }
    }

    fun scale(origin: Bitmap, maxWidthAndHeight:Int):Bitmap{
        var newWidth = 0
        var newHeight = 0

        if (origin.width >= origin.height){
            val ratio:Float = origin.width.toFloat() / origin.height.toFloat()

            newWidth = maxWidthAndHeight
            // Calculate the new height for the scaled bitmap
            newHeight = Math.round(maxWidthAndHeight / ratio)
        }else{
            val ratio:Float = origin.height.toFloat() / origin.width.toFloat()

            // Calculate the new width for the scaled bitmap
            newWidth = Math.round(maxWidthAndHeight / ratio)
            newHeight = maxWidthAndHeight
        }

        return Bitmap.createScaledBitmap(
            origin,
            newWidth,
            newHeight,
            false
        )
    }
}