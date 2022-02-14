package akio.apps.myrun.feature.core.measurement

import akio.apps.myrun.data.user.api.UnitConverter
import akio.apps.myrun.data.user.api.model.UserProfile
import akio.apps.myrun.feature.core.R
import androidx.annotation.StringRes

abstract class UserProfileUnitFormatter<RAW>(
    @StringRes labelResId: Int,
    @StringRes unitResId: Int? = null,
    converter: UnitConverter<RAW, Double>,
) : UnitFormatter<RAW>(labelResId, unitResId, converter) {

    abstract fun getFormattedValue(userProfile: UserProfile): String

    abstract class BodyWeightUnitFormatter(
        @StringRes unitResId: Int? = null,
        converter: UnitConverter<Float, Double>,
    ) : UserProfileUnitFormatter<Float>(
        R.string.user_profile_unit_weight_label,
        unitResId,
        converter
    ) {
        override fun getFormattedValue(value: Float): String =
            String.format("%.1f", converter.fromRawValue(value))

        override fun getFormattedValue(userProfile: UserProfile): String =
            getFormattedValue(userProfile.weight)
    }

    internal object WeightKg : BodyWeightUnitFormatter(
        unitResId = R.string.user_profile_unit_weight_kg,
        UnitConverter.FloatRaw
    )

    internal object WeightPound : BodyWeightUnitFormatter(
        unitResId = R.string.user_profile_unit_weight_lbs,
        UnitConverter.WeightPound
    )
}
