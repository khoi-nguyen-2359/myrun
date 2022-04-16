package akio.apps.myrun.feature.base.ktx

import android.app.Activity
import android.os.Parcelable

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
