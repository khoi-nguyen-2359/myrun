package akio.apps.myrun.feature.core.measurement

import akio.apps.myrun.data.user.api.UnitConverter
import akio.apps.myrun.feature.core.R
import androidx.annotation.StringRes

abstract class StatsUnitFormatter<RAW : Number>(
    @StringRes labelResId: Int,
    @StringRes unitResId: Int,
    converter: UnitConverter<RAW, Double>,
) : UnitFormatter<RAW>(labelResId, unitResId, converter) {
    internal object DurationKm : StatsUnitFormatter<Double>(
        R.string.route_tracking_distance_label,
        R.string.performance_unit_distance_km,
        UnitConverter.DistanceKm
    ) {
        override fun getFormattedValue(value: Double): String =
            String.format("%.1f", converter.fromRawValue(value))
    }

    internal object DurationMile : StatsUnitFormatter<Double>(
        R.string.route_tracking_distance_label,
        R.string.performance_unit_distance_mi,
        UnitConverter.DistanceMile
    ) {
        override fun getFormattedValue(value: Double): String =
            String.format("%.1f", converter.fromRawValue(value))
    }

    internal object TimeHour : StatsUnitFormatter<Long>(
        R.string.performance_duration_label,
        R.string.performance_unit_time_hour,
        UnitConverter.TimeHour
    ) {
        override fun getFormattedValue(value: Long): String =
            String.format("%.1f", converter.fromRawValue(value))
    }
}
