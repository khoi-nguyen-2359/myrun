package akio.apps.common.feature.ui

import android.app.Activity
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

@Suppress("UNCHECKED_CAST")
fun <T> Activity.lazyExtras(
    name: String,
    default: T
): Lazy<T> = lazy {
    when (default) {
        is Int -> intent.getIntExtra(name, default)
        is Long -> intent.getLongExtra(name, default)
        is Boolean -> intent.getBooleanExtra(name, default)
        is String -> intent.getStringExtra(name) ?: default
        else -> throw IllegalArgumentException("Can not determine extra value type.")
    } as T
}
