package akio.apps._base.ktext

import android.content.Context
import android.graphics.drawable.Drawable
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat

fun Context.getColorCompat(@ColorRes color: Int): Int {
    return ContextCompat.getColor(this, color)
}

fun Context.getDrawableCompat(@DrawableRes drawable: Int): Drawable? {
    return ContextCompat.getDrawable(this, drawable)
}
