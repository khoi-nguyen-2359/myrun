package akio.apps.myrun.feature.base

import akio.apps.myrun.data.activity.api.model.BaseActivityModel
import akio.apps.myrun.data.activity.api.model.CyclingActivityModel
import akio.apps.myrun.data.activity.api.model.RunningActivityModel
import akio.apps.myrun.domain.common.TrackingValueConverter
import android.content.Context
import androidx.annotation.StringRes

sealed class TrackingValueFormatter<T : Number>(
    val id: String,
    @StringRes val labelResId: Int,
    @StringRes val unitResId: Int? = null,
) {

    fun getUnit(context: Context): String = if (unitResId != null) {
        context.getString(unitResId)
    } else {
        ""
    }

    fun getLabel(context: Context): String = context.getString(labelResId)

    abstract fun getFormattedValue(activity: BaseActivityModel): String
    abstract fun getFormattedValue(value: T): String

    object DistanceKm :
        TrackingValueFormatter<Double>(
            "DistanceKm",
            R.string.route_tracking_distance_label,
            R.string.performance_unit_distance_km
        ) {
        override fun getFormattedValue(activity: BaseActivityModel): String =
            getFormattedValue(activity.distance)

        override fun getFormattedValue(value: Double): String =
            String.format("%.2f", TrackingValueConverter.DistanceKm.fromRawValue(value))
    }

    object PaceMinutePerKm : TrackingValueFormatter<Double>(
        "PaceMinutePerKm",
        R.string.route_tracking_pace_label,
        R.string.performance_unit_pace_min_per_km
    ) {
        override fun getFormattedValue(activity: BaseActivityModel): String =
            getFormattedValue((activity as? RunningActivityModel)?.pace ?: 0.0)

        override fun getFormattedValue(value: Double): String {
            val intMinute = value.toInt()
            val second = (value - intMinute) * 60
            return String.format("%d:%02d", intMinute, second.toInt())
        }
    }

    object SpeedKmPerHour :
        TrackingValueFormatter<Double>(
            "SpeedKmPerHour",
            R.string.route_tracking_speed_label,
            R.string.performance_unit_speed
        ) {
        override fun getFormattedValue(activity: BaseActivityModel): String =
            getFormattedValue((activity as? CyclingActivityModel)?.speed ?: 0.0)

        override fun getFormattedValue(value: Double): String = String.format("%.2f", value)
    }

    object DurationHourMinuteSecond :
        TrackingValueFormatter<Long>(
            "DurationHourMinuteSecond",
            R.string.performance_duration_label
        ) {
        override fun getFormattedValue(activity: BaseActivityModel): String =
            getFormattedValue(activity.duration)

        override fun getFormattedValue(value: Long): String {
            val hour = TrackingValueConverter.TimeHour.fromRawValue(value)
            val min = (hour - hour.toInt()) * 60
            val sec = (min - min.toInt()) * 60
            return if (hour < 1) {
                String.format("%d:%02d", min.toInt(), sec.toInt())
            } else {
                String.format("%d:%02d:%02d", hour.toInt(), min.toInt(), sec.toInt())
            }
        }
    }
}
