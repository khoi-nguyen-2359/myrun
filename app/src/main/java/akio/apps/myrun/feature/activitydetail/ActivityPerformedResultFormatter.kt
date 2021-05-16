package akio.apps.myrun.feature.activitydetail

import akio.apps.myrun.R
import akio.apps.myrun.feature.usertimeline.model.Activity
import akio.apps.myrun.feature.usertimeline.model.CyclingActivity
import akio.apps.myrun.feature.usertimeline.model.RunningActivity
import android.content.Context
import androidx.annotation.StringRes

enum class ActivityPerformedResultFormatter(
    @StringRes val labelResId: Int,
    val performedValueFormatter: PerformedValueFormatter
) {
    Distance(
        labelResId = R.string.route_tracking_distance_label,
        performedValueFormatter = PerformedValueFormatter.DistanceKm
    ) {
        override fun getPerformedResultValue(activity: Activity): Number = activity.distance
    },
    Pace(
        labelResId = R.string.performance_avg_pace_label,
        performedValueFormatter = PerformedValueFormatter.PaceMinutePerKm
    ) {
        override fun getPerformedResultValue(activity: Activity): Number =
            (activity as? RunningActivity)?.pace ?: 0
    },
    Speed(
        labelResId = R.string.performance_speed_label,
        performedValueFormatter = PerformedValueFormatter.SpeedKmPerHour
    ) {
        override fun getPerformedResultValue(activity: Activity): Number =
            (activity as? CyclingActivity)?.speed ?: 0
    },
    Duration(
        labelResId = R.string.performance_duration_label,
        performedValueFormatter = PerformedValueFormatter.DurationHourMinuteSecond
    ) {
        override fun getPerformedResultValue(activity: Activity): Number = activity.duration
    };

    protected abstract fun getPerformedResultValue(activity: Activity): Number
    fun getFormattedPerformedResultValue(activity: Activity) =
        performedValueFormatter.formatRawValue(getPerformedResultValue(activity))
    fun getUnit(context: Context): String = performedValueFormatter.getUnit(context)
    fun getLabel(context: Context): String = context.getString(labelResId)
}
