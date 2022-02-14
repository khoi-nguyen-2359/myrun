package akio.apps.myrun.feature.core.measurement

import akio.apps.myrun.data.activity.api.model.BaseActivityModel
import akio.apps.myrun.data.activity.api.model.CyclingActivityModel
import akio.apps.myrun.data.activity.api.model.RunningActivityModel
import akio.apps.myrun.data.user.api.UnitConverter
import akio.apps.myrun.feature.core.R
import androidx.annotation.StringRes

/**
 * Same idea with [UnitConverter] but for UI presentation purposes.
 */
abstract class TrackUnitFormatter<RAW : Number>(
    @StringRes labelResId: Int,
    @StringRes unitResId: Int? = null,
    converter: UnitConverter<RAW, Double>,
) : UnitFormatter<RAW>(labelResId, unitResId, converter) {

    abstract fun getFormattedValue(activity: BaseActivityModel): String

    abstract class DistanceUnitFormatter(
        unitResId: Int? = null,
        converter: UnitConverter<Double, Double>,
    ) : TrackUnitFormatter<Double>(
        R.string.route_tracking_distance_label,
        unitResId,
        converter
    ) {
        override fun getFormattedValue(value: Double): String =
            String.format("%.2f", converter.fromRawValue(value))

        override fun getFormattedValue(activity: BaseActivityModel): String =
            getFormattedValue(activity.distance)
    }

    internal object DistanceKm : DistanceUnitFormatter(
        R.string.performance_unit_distance_km,
        UnitConverter.DistanceKm
    )

    internal object DistanceMile : DistanceUnitFormatter(
        R.string.performance_unit_distance_mi,
        UnitConverter.DistanceMile
    )

    abstract class PaceUnitFormatter(
        unitResId: Int? = null,
        converter: UnitConverter<Double, Double>,
    ) : TrackUnitFormatter<Double>(R.string.route_tracking_pace_label, unitResId, converter) {
        override fun getFormattedValue(value: Double): String {
            val intMinute = value.toInt()
            val second = (value - intMinute) * 60
            return String.format("%d:%02d", intMinute, second.toInt())
        }

        override fun getFormattedValue(activity: BaseActivityModel): String {
            val rawValue = (activity as? RunningActivityModel)?.pace ?: 0.0
            return getFormattedValue(converter.fromRawValue(rawValue))
        }
    }

    internal object PaceMinutePerKm : PaceUnitFormatter(
        R.string.performance_unit_pace_min_per_km,
        UnitConverter.DoubleRaw
    )

    internal object PaceMinutePerMile : PaceUnitFormatter(
        R.string.performance_unit_pace_min_per_mile,
        UnitConverter.PaceMinutePerMile
    )

    abstract class SpeedUnitFormatter(
        unitResId: Int? = null,
        converter: UnitConverter<Double, Double>,
    ) : TrackUnitFormatter<Double>(
        R.string.route_tracking_speed_label,
        unitResId,
        converter
    ) {
        override fun getFormattedValue(activity: BaseActivityModel): String {
            val rawValue = (activity as? CyclingActivityModel)?.speed ?: 0.0
            return getFormattedValue(converter.fromRawValue(rawValue))
        }

        override fun getFormattedValue(value: Double): String = String.format("%.2f", value)
    }

    internal object SpeedKmPerHour : SpeedUnitFormatter(
        R.string.performance_unit_speed_km_per_hour,
        UnitConverter.DoubleRaw
    )

    internal object SpeedMilePerHour : SpeedUnitFormatter(
        R.string.performance_unit_speed_mi_per_hour,
        UnitConverter.SpeedMilePerHour
    )

    abstract class DurationUnitFormatter(
        @StringRes unitResId: Int? = null,
        converter: UnitConverter<Long, Double>,
    ) : TrackUnitFormatter<Long>(R.string.performance_duration_label, unitResId, converter) {
        override fun getFormattedValue(activity: BaseActivityModel): String =
            getFormattedValue(activity.duration)

        override fun getFormattedValue(value: Long): String {
            val hour = DurationHourMinuteSecond.converter.fromRawValue(value)
            val min = (hour - hour.toInt()) * 60
            val sec = (min - min.toInt()) * 60
            return if (hour < 1) {
                String.format("%d:%02d", min.toInt(), sec.toInt())
            } else {
                String.format("%d:%02d:%02d", hour.toInt(), min.toInt(), sec.toInt())
            }
        }
    }

    internal object DurationHourMinuteSecond : DurationUnitFormatter(
        unitResId = null,
        UnitConverter.TimeHour
    )
}
