package akio.apps.myrun.feature.activitydetail

import akio.apps.myrun.R
import akio.apps.myrun.domain.TrackingValueConverter
import akio.apps.myrun.feature.usertimeline.model.Activity
import akio.apps.myrun.feature.usertimeline.model.CyclingActivity
import akio.apps.myrun.feature.usertimeline.model.RunningActivity
import android.content.Context
import androidx.annotation.StringRes

sealed class TrackingValueFormatter(
    val id: String,
    @StringRes val labelResId: Int,
    @StringRes val unitResId: Int? = null
) {

    fun getUnit(context: Context): String = if (unitResId != null) {
        context.getString(unitResId)
    } else {
        ""
    }

    fun getLabel(context: Context): String = context.getString(labelResId)

    abstract fun getFormattedValue(activity: Activity): String

    object DistanceKm :
        TrackingValueFormatter(
            "DistanceKm",
            R.string.route_tracking_distance_label,
            R.string.performance_unit_distance_km
        ) {
        override fun getFormattedValue(activity: Activity): String {
            val distance = activity.distance
            return String.format("%.2f", TrackingValueConverter.DistanceKm.fromRawValue(distance))
        }
    }

    object PaceMinutePerKm : TrackingValueFormatter(
        "PaceMinutePerKm",
        R.string.route_tracking_pace_label,
        R.string.performance_unit_pace_min_per_km
    ) {
        override fun getFormattedValue(activity: Activity): String {
            val minute = (activity as? RunningActivity)?.pace ?: 0.0
            val intMinute = minute.toInt()
            val second = (minute - intMinute) * 60
            return "$intMinute:${second.toInt()}"
        }
    }

    object SpeedKmPerHour :
        TrackingValueFormatter(
            "SpeedKmPerHour",
            R.string.route_tracking_speed_label,
            R.string.performance_unit_speed
        ) {
        override fun getFormattedValue(activity: Activity): String =
            "${(activity as? CyclingActivity)?.speed ?: 0.0}"
    }

    object DurationHourMinuteSecond :
        TrackingValueFormatter("DurationHourMinuteSecond", R.string.performance_duration_label) {
        override fun getFormattedValue(activity: Activity): String {
            val millisecond = activity.duration
            val hour = TrackingValueConverter.TimeHour.fromRawValue(millisecond).toInt()
            val min = TrackingValueConverter.TimeMinute.fromRawValue(millisecond).toInt()
            val sec = TrackingValueConverter.TimeSecond.fromRawValue(millisecond).toInt()
            return if (hour == 0) {
                String.format("%d:%02d", min, sec)
            } else {
                String.format("%d:%02d:%02d", hour, min, sec)
            }
        }
    }
}
