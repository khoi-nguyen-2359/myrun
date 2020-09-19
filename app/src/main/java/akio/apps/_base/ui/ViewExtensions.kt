package akio.apps._base.ui

import android.content.Context
import android.content.res.Resources
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.LayoutRes
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView

val Int.px2dp: Float
	get() = this / Resources.getSystem().displayMetrics.density

val Int.dp2px: Float
	get() = this * Resources.getSystem().displayMetrics.density

fun RectF.toRect() = Rect(left.toInt(), top.toInt(), right.toInt(), bottom.toInt())

fun RecyclerView.ViewHolder.getString(@StringRes resId: Int) = itemView.context.getString(resId)

fun RecyclerView.ViewHolder.getString(@StringRes resId: Int, vararg formatArgs: Any) = itemView.context.getString(resId, *formatArgs)

fun RecyclerView.ViewHolder.getContext(): Context = itemView.context

fun RecyclerView.ViewHolder.getResources(): Resources = itemView.resources

fun ViewGroup.inflate(@LayoutRes layoutResId: Int, attachToRoot: Boolean = false)
		= LayoutInflater.from(context).inflate(layoutResId, this, attachToRoot)

fun TextView.getTextAsString() = text.toString()

fun TextView.getNoneEmptyTextOrNull(): String? {
	return if (text.isEmpty())
		null
	else
		text.toString()
}

fun Context.getColorCompat(@ColorRes color: Int): Int {
	return ContextCompat.getColor(this, color)
}

fun Context.getDrawableCompat(@DrawableRes drawable: Int): Drawable? {
	return ContextCompat.getDrawable(this, drawable)
}

fun View.setVisibleOrGone(visibleOrGone: Boolean) {
	visibility = if (visibleOrGone) View.VISIBLE
	else View.GONE
}

fun Array<out View>.setVisibleOrGone(visibleOrGone: Boolean) {
	for (view in this) {
		if (visibleOrGone)
			view.visibility = View.VISIBLE
		else
			view.visibility = View.GONE
	}
}

fun View.setVisibleOrInvisible(visibleOrInvisible: Boolean) {
	if (visibleOrInvisible) visibility = View.VISIBLE
	else visibility = View.INVISIBLE
}