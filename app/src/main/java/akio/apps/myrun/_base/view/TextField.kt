package akio.apps.myrun._base.view

import akio.apps.myrun.R
import android.content.Context
import android.graphics.Typeface.BOLD
import android.graphics.Typeface.NORMAL
import android.text.SpannableString
import android.text.Spanned
import android.text.style.StyleSpan
import android.text.style.TextAppearanceSpan
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView
import java.util.Locale

class TextField @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatTextView(context, attrs, defStyleAttr) {

    private val label: String
    private var value: String? = null

    init {
        val styledAttrs = context.obtainStyledAttributes(attrs, R.styleable.TextField)
        label = styledAttrs.getString(R.styleable.TextField_tf_label) ?: ""
        value = styledAttrs.getString(R.styleable.TextField_tf_value) ?: " "
        styledAttrs.recycle()

        setValue(value)
    }

    fun setValue(value: String?) {
        this.value = value
        val spannedValue =
            SpannableString("${label.uppercase(Locale.getDefault())}\n${value ?: ""}")
        val labelTfSpan = StyleSpan(BOLD)
        spannedValue.setSpan(labelTfSpan, 0, label.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        val labelTextSpan = TextAppearanceSpan(context, R.style.TextFieldLabel)
        spannedValue.setSpan(labelTextSpan, 0, label.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

        if (value?.isNotEmpty() == true) {
            val valueSpan = StyleSpan(NORMAL)
            spannedValue.setSpan(
                valueSpan,
                label.length + 1,
                spannedValue.length,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            val valueTextSpan = TextAppearanceSpan(context, R.style.TextFieldValue)
            spannedValue.setSpan(
                valueTextSpan,
                label.length + 1,
                spannedValue.length,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }

        text = spannedValue
    }

    fun getValue() = value
}
