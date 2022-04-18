package akio.apps.myrun.feature.core

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.annotation.DrawableRes
import androidx.appcompat.content.res.AppCompatResources

object BitmapUtils {
    fun createDrawableBitmap(
        context: Context,
        @DrawableRes drawableResId: Int,
    ): Bitmap? {
        val drawable = AppCompatResources.getDrawable(context, drawableResId) ?: return null
        drawable.setBounds(0, 0, drawable.intrinsicWidth, drawable.intrinsicHeight)
        val bitmap = Bitmap.createBitmap(
            drawable.intrinsicWidth,
            drawable.intrinsicHeight,
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        drawable.draw(canvas)
        return bitmap
    }
}
