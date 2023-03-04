package akio.apps.myrun.feature.core.ktx

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Parcelable

@Suppress("UNCHECKED_CAST")
inline fun <reified T> Activity.extra(name: String, default: T): T = when (T::class) {
    Int::class -> intent.getIntExtra(name, default as Int)
    Long::class -> intent.getLongExtra(name, default as Long)
    Boolean::class -> intent.getBooleanExtra(name, default as Boolean)
    String::class -> intent.getStringExtra(name) ?: default
    Parcelable::class -> intent.getParcelableExtraExt(name, default)
    else -> throw IllegalArgumentException("Can not determine extra value type.")
} as T

inline fun <reified T> Intent.getParcelableExtraExt(name: String, default: T? = null) =
    if (Build.VERSION.SDK_INT >= 33) {
        getParcelableExtra(name, T::class.java)
    } else {
        @Suppress("DEPRECATION")
        getParcelableExtra(name)
    } ?: default
