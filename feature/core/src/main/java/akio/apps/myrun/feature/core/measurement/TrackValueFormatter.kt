package akio.apps.myrun.feature.core.measurement

import akio.apps.myrun.data.activity.api.model.BaseActivityModel
import akio.apps.myrun.data.activity.api.model.CyclingActivityModel
import akio.apps.myrun.data.activity.api.model.RunningActivityModel
import akio.apps.myrun.data.user.api.UnitConverter
import akio.apps.myrun.data.user.api.model.MeasureSystem
import akio.apps.myrun.feature.core.R
import android.content.Context
import androidx.annotation.StringRes

/**
 * Same idea with [TrackValueConverter] but for UI presentation purposes.
 */
sealed class TrackValueFormatter<R : Number>(
    @StringRes val labelResId: Int,
    @StringRes val unitResId: Int? = null,
    val converter: UnitConverter<R, Double>,
) {

    fun getUnit(context: Context): String = if (unitResId != null) {
        context.getString(unitResId)
    } else {
        ""
    }

    fun getLabel(context: Context): String = context.getString(labelResId)

    abstract fun getFormattedValue(activity: BaseActivityModel): String
    abstract fun getFormattedValue(value: R): String

    private sealed class Distance(
        unitResId: Int? = null,
        converter: UnitConverter<Double, Double>,
    ) : TrackValueFormatter<Double>(
        R.string.route_tracking_distance_label,
        unitResId,
        converter
    ) {
        override fun getFormattedValue(value: Double): String =
            String.format("%.2f", converter.fromRawValue(value))
    }

    private object DistanceKm : Distance(
        R.string.performance_unit_distance_km,
        UnitConverter.DistanceKm
    ) {
        override fun getFormattedValue(activity: BaseActivityModel): String =
            getFormattedValue(activity.distance)
    }

    private object DistanceMile : Distance(
        R.string.performance_unit_distance_mi,
        UnitConverter.DistanceMile
    ) {
        override fun getFormattedValue(activity: BaseActivityModel): String =
            getFormattedValue(activity.distance)
    }

    private sealed class PaceMinute(
        unitResId: Int? = null,
        converter: UnitConverter<Double, Double>,
    ) : TrackValueFormatter<Double>(R.string.route_tracking_pace_label, unitResId, converter) {
        override fun getFormattedValue(value: Double): String {
            val intMinute = value.toInt()
            val second = (value - intMinute) * 60
            return String.format("%d:%02d", intMinute, second.toInt())
        }
    }

    private object PaceMinutePerKm : PaceMinute(
        R.string.performance_unit_pace_min_per_km,
        UnitConverter.DoubleRaw
    ) {
        override fun getFormattedValue(activity: BaseActivityModel): String {
            val rawValue = (activity as? RunningActivityModel)?.pace ?: 0.0
            return getFormattedValue(converter.fromRawValue(rawValue))
        }
    }

    private object PaceMinutePerMile : PaceMinute(
        R.string.performance_unit_pace_min_per_mile,
        UnitConverter.PaceMinutePerMile
    ) {
        override fun getFormattedValue(activity: BaseActivityModel): String {
            val rawValue = (activity as? RunningActivityModel)?.pace ?: 0.0
            return getFormattedValue(converter.fromRawValue(rawValue))
        }
    }

    private sealed class SpeedPerHour(
        unitResId: Int? = null,
        converter: UnitConverter<Double, Double>,
    ) : TrackValueFormatter<Double>(
        R.string.route_tracking_speed_label,
        unitResId,
        converter
    ) {
        override fun getFormattedValue(value: Double): String = String.format("%.2f", value)
    }

    private object SpeedKmPerHour : SpeedPerHour(
        R.string.performance_unit_speed_km_per_hour,
        UnitConverter.DoubleRaw
    ) {
        override fun getFormattedValue(activity: BaseActivityModel): String {
            val rawValue = (activity as? CyclingActivityModel)?.speed ?: 0.0
            return getFormattedValue(converter.fromRawValue(rawValue))
        }
    }

    private object SpeedMilePerHour : SpeedPerHour(
        R.string.performance_unit_speed_mi_per_hour,
        UnitConverter.SpeedMilePerHour
    ) {
        override fun getFormattedValue(activity: BaseActivityModel): String {
            val rawValue = (activity as? CyclingActivityModel)?.speed ?: 0.0
            return getFormattedValue(converter.fromRawValue(rawValue))
        }
    }

    private object DurationHourMinuteSecond : TrackValueFormatter<Long>(
        R.string.performance_duration_label,
        unitResId = null,
        UnitConverter.TimeHour
    ) {
        override fun getFormattedValue(activity: BaseActivityModel): String =
            getFormattedValue(activity.duration)

        override fun getFormattedValue(value: Long): String {
            val hour = converter.fromRawValue(value)
            val min = (hour - hour.toInt()) * 60
            val sec = (min - min.toInt()) * 60
            return if (hour < 1) {
                String.format("%d:%02d", min.toInt(), sec.toInt())
            } else {
                String.format("%d:%02d:%02d", hour.toInt(), min.toInt(), sec.toInt())
            }
        }
    }

    companion object {
        fun createFormatterPreference(
            measureSystem: MeasureSystem,
        ): TrackValueFormatPreference =
            when (measureSystem) {
                MeasureSystem.Metric -> TrackValueFormatPreference(
                    DistanceKm,
                    PaceMinutePerKm,
                    SpeedKmPerHour,
                    DurationHourMinuteSecond
                )
                MeasureSystem.Imperial -> TrackValueFormatPreference(
                    DistanceMile,
                    PaceMinutePerMile,
                    SpeedMilePerHour,
                    DurationHourMinuteSecond
                )
            }
    }
}
