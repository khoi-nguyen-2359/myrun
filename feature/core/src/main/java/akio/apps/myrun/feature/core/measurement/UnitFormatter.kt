package akio.apps.myrun.feature.core.measurement

import akio.apps.myrun.data.user.api.UnitConverter
import android.content.Context
import androidx.annotation.StringRes

abstract class UnitFormatter<RAW>(
    @StringRes val labelResId: Int,
    @StringRes val unitResId: Int? = null,
    val converter: UnitConverter<RAW, Double>,
) {

    fun getUnit(context: Context): String = if (unitResId != null) {
        context.getString(unitResId)
    } else {
        ""
    }

    fun getLabel(context: Context): String = context.getString(labelResId)

    abstract fun getFormattedValue(value: RAW): String
}
