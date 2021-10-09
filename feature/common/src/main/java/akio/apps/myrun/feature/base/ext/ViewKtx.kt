package akio.apps.myrun.feature.base.ext

import android.content.res.Resources
import android.graphics.Rect
import android.graphics.RectF
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.LayoutRes

val Int.px2dp: Float
    get() = this / Resources.getSystem().displayMetrics.density

val Int.dp2px: Float
    get() = this * Resources.getSystem().displayMetrics.density

fun RectF.toRect() = Rect(left.toInt(), top.toInt(), right.toInt(), bottom.toInt())

fun ViewGroup.inflate(@LayoutRes layoutResId: Int, attachToRoot: Boolean = false) =
    LayoutInflater.from(context)
        .inflate(layoutResId, this, attachToRoot)

fun TextView.getTextAsString() = text.toString()

fun TextView.getNoneEmptyTextOrNull(): String? {
    return if (text.isEmpty())
        null
    else
        text.toString()
}

fun View.setVisibleOrGone(visibleOrGone: Boolean) {
    visibility = if (visibleOrGone) View.VISIBLE
    else View.GONE
}
