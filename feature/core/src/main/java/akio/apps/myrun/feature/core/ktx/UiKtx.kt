package akio.apps.myrun.feature.core.ktx

import android.content.res.Resources
import android.graphics.Rect
import android.graphics.RectF

val Int.px2dp: Float
    get() = this / Resources.getSystem().displayMetrics.density

val Int.dp2px: Float
    get() = this * Resources.getSystem().displayMetrics.density

fun RectF.toRect() = Rect(left.toInt(), top.toInt(), right.toInt(), bottom.toInt())
