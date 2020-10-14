package akio.apps.myrun._base.view

import akio.apps._base.ui.dp2px
import akio.apps._base.ui.getDrawableCompat
import akio.apps.myrun.R
import android.content.Context
import android.graphics.Typeface.BOLD
import android.graphics.Typeface.NORMAL
import android.text.SpannableString
import android.text.Spanned
import android.text.style.StyleSpan
import android.text.style.TextAppearanceSpan
import android.util.AttributeSet
import android.view.Gravity
import androidx.appcompat.widget.AppCompatTextView

class TextField @JvmOverloads constructor(
	context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : AppCompatTextView(context, attrs, defStyleAttr) {
	
	private val label: String
	private var value: String? = null

//	private val labelTypeface by lazy { ResourcesCompat.getFont(context, R.font.condensed_bold)!! }
//	private val valueTypeface by lazy { ResourcesCompat.getFont(context, R.font.condensed_bold_obl)!! }
	
	init {
//		background = context.getDrawableCompat(R.drawable.text_field_background_selector)
		val horizontalPadding = resources.getDimensionPixelSize(R.dimen.common_page_horizontal_padding)
		val verticalPadding = resources.getDimensionPixelSize(R.dimen.common_page_vertical_padding)
		setPadding(horizontalPadding, verticalPadding, horizontalPadding, verticalPadding)
		setLineSpacing(5.dp2px, 1f)
		gravity = Gravity.CENTER_VERTICAL
		compoundDrawablePadding = horizontalPadding
		
		val styledAttrs = context.obtainStyledAttributes(attrs, R.styleable.TextField)
		label = styledAttrs.getString(R.styleable.TextField_tf_label) ?: ""
		styledAttrs.recycle()
		
		setValue("")
	}
	
	fun setValue(value: String?) {
		this.value = value
		val spannedValue = SpannableString("${label.toUpperCase()}\n${value ?: ""}")
		val labelTfSpan = StyleSpan(BOLD)
		spannedValue.setSpan(labelTfSpan, 0, label.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
		val labelTextSpan = TextAppearanceSpan(context, R.style.TextFieldLabel)
		spannedValue.setSpan(labelTextSpan, 0, label.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
		
		if (value?.isNotEmpty() == true) {
			val valueSpan = StyleSpan(NORMAL)
			spannedValue.setSpan(valueSpan, label.length + 1, spannedValue.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
			val valueTextSpan = TextAppearanceSpan(context, R.style.TextFieldValue)
			spannedValue.setSpan(valueTextSpan, label.length + 1, spannedValue.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
		}
		
		text = spannedValue
	}
	
	fun getValue() = value
}