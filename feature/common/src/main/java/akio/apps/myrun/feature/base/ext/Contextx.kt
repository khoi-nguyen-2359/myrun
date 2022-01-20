package akio.apps.myrun.feature.base.ext

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.os.Parcelable
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
inline fun <reified T> Activity.extra(
    name: String,
    default: T,
): T = when (T::class) {
    Int::class -> intent.getIntExtra(name, default as Int)
    Long::class -> intent.getLongExtra(name, default as Long)
    Boolean::class -> intent.getBooleanExtra(name, default as Boolean)
    String::class -> intent.getStringExtra(name) ?: default
    Parcelable::class -> intent.getParcelableExtra(name) ?: default
    else -> throw IllegalArgumentException("Can not determine extra value type.")
} as T

fun Context.checkSelfPermissions(permissions: Array<String>): Boolean {
    return permissions.all { permission ->
        ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
    }
}
