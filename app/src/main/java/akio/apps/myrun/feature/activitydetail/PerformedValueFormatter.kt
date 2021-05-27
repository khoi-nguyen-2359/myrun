package akio.apps.myrun.feature.activitydetail

import akio.apps.myrun.R
import akio.apps.myrun.domain.PerformanceUnit
import android.content.Context
import androidx.annotation.StringRes

enum class PerformedValueFormatter(val id: String, @StringRes val unitResId: Int? = null) {
    DistanceKm("DistanceKm", R.string.performance_unit_distance_km) {
        override fun formatRawValue(rawValue: Number): String =
            String.format(
                "%.2f",
                PerformanceUnit.DistanceKm.fromRawValue(rawValue)
            )
    },
    PaceMinutePerKm("PaceMinutePerKm", R.string.performance_unit_pace_min_per_km) {
        override fun formatRawValue(rawValue: Number): String {
            val minute = PerformanceUnit.PaceMinutePerKm.fromRawValue(rawValue)
            val intSecond = ((minute - minute.toInt()) * 60).toInt()
            val intMinute = minute.toInt()
            return "$intMinute:$intSecond"
        }
    },
    SpeedKmPerHour("SpeedKmPerHour", R.string.performance_unit_speed) {
        override fun formatRawValue(rawValue: Number): String =
            "${PerformanceUnit.SpeedKmPerHour.fromRawValue(rawValue)}"
    },
    DurationHourMinuteSecond("DurationHourMinuteSecond") {
        override fun formatRawValue(rawValue: Number): String {
            val millisecond = PerformanceUnit.TimeMillisecond.fromRawValue(rawValue)
            val hour = millisecond / 3600000
            val min = (millisecond % 3600000) / 60000
            val sec = ((millisecond % 3600000) % 60000) / 1000
            return if (hour == 0L) {
                String.format("%d:%02d", min, sec)
            } else {
                String.format("%d:%02d:%02d", hour, min, sec)
            }
        }
    },
    CadenceStepPerMinute("CadenceStepPerMinute", R.string.performance_cadence_unit) {
        override fun formatRawValue(rawValue: Number): String {
            val spm = PerformanceUnit.CadenceStepPerMinute.fromRawValue(rawValue)
            return "$spm"
        }
    };

    abstract fun formatRawValue(rawValue: Number): String
    fun getUnit(context: Context): String = if (unitResId != null) {
        context.getString(unitResId)
    } else {
        ""
    }
}
