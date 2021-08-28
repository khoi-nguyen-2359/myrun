package akio.apps.myrun.feature.activitydetail

import akio.apps.myrun.data.activity.api.model.ActivityModel
import akio.apps.myrun.data.activity.api.model.CyclingActivityModel
import akio.apps.myrun.data.activity.api.model.RunningActivityModel
import akio.apps.myrun.domain.TrackingValueConverter
import android.content.Context
import androidx.annotation.StringRes

sealed class TrackingValueFormatter(
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

    abstract fun getFormattedValue(activity: ActivityModel): String

    object DistanceKm :
        TrackingValueFormatter(
            "DistanceKm",
            R.string.route_tracking_distance_label,
            R.string.performance_unit_distance_km
        ) {
        override fun getFormattedValue(activity: ActivityModel): String {
            val distance = activity.distance
            return String.format("%.2f", TrackingValueConverter.DistanceKm.fromRawValue(distance))
        }
    }

    object PaceMinutePerKm : TrackingValueFormatter(
        "PaceMinutePerKm",
        R.string.route_tracking_pace_label,
        R.string.performance_unit_pace_min_per_km
    ) {
        override fun getFormattedValue(activity: ActivityModel): String {
            val minute = (activity as? RunningActivityModel)?.pace ?: 0.0
            val intMinute = minute.toInt()
            val second = (minute - intMinute) * 60
            return String.format("%d:%02d", intMinute, second.toInt())
        }
    }

    object SpeedKmPerHour :
        TrackingValueFormatter(
            "SpeedKmPerHour",
            R.string.route_tracking_speed_label,
            R.string.performance_unit_speed
        ) {
        override fun getFormattedValue(activity: ActivityModel): String =
            String.format("%.2f", (activity as? CyclingActivityModel)?.speed ?: 0.0)
    }

    object DurationHourMinuteSecond :
        TrackingValueFormatter("DurationHourMinuteSecond", R.string.performance_duration_label) {
        override fun getFormattedValue(activity: ActivityModel): String {
            val millisecond = activity.duration
            val hour = TrackingValueConverter.TimeHour.fromRawValue(millisecond)
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
