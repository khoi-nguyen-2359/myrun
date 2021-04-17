package akio.apps.myrun.feature.activitydetail.ui

import akio.apps._base.Resource
import akio.apps.myrun.R
import akio.apps.myrun.domain.PerformanceUnit
import akio.apps.myrun.feature.activitydetail.ActivityDateTimeFormatter
import akio.apps.myrun.feature.activitydetail.ActivityDetailViewModel
import akio.apps.myrun.feature.usertimeline.model.Activity
import akio.apps.myrun.feature.usertimeline.model.CyclingActivity
import akio.apps.myrun.feature.usertimeline.model.RunningActivity
import android.content.Context
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import com.google.accompanist.glide.GlideImage

@Composable
fun ActivityDetailComposable(activityDetailViewModel: ActivityDetailViewModel) {
    val activityResource by activityDetailViewModel.activityDetails.collectAsState(
        Resource.Loading()
    )
    val activityFormattedStartTime by activityDetailViewModel.activityDateTime.collectAsState(
        ActivityDateTimeFormatter.Result.FullDateTime("")
    )

    val activityDetail = activityResource.data
    if (activityDetail != null) {
        Column {
            ActivityInfoHeaderComposable(activityDetail, activityFormattedStartTime)
            GlideImage(
                data = activityDetail.routeImage,
                contentDescription = "Activity route image",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(ratio = 1.5f)
            )
            PerformanceTableComposable(
                createDistancePerformedResult(LocalContext.current, activityDetail),
                createSpeedOrPacePerformedResult(LocalContext.current, activityDetail),
                createActivityDurationPerformedResult(LocalContext.current, activityDetail)
            )
        }
    }
}

fun createActivityDurationPerformedResult(
    context: Context,
    activityDetail: Activity
): PerformedResult = PerformedResult(
    label = context.getString(R.string.performance_duration_label),
    formattedValue = PerformedValueFormatter.DurationHourMinuteSecond.formatRawValue(
        context,
        activityDetail.duration.toDouble()
    )
)

fun createSpeedOrPacePerformedResult(context: Context, activityDetail: Activity): PerformedResult =
    when (activityDetail) {
        is RunningActivity -> PerformedResult(
            label = context.getString(R.string.performance_avg_pace_label),
            formattedValue = PerformedValueFormatter.PaceMinutePerKm.formatRawValue(
                context,
                activityDetail.pace
            )
        )
        is CyclingActivity -> PerformedResult(
            label = context.getString(R.string.performance_speed_label),
            formattedValue = PerformedValueFormatter.SpeedKmPerHour.formatRawValue(
                context,
                activityDetail.speed
            )
        )
        else -> throw Exception("")
    }

fun createDistancePerformedResult(context: Context, activityDetail: Activity): PerformedResult =
    PerformedResult(
        label = context.getString(R.string.route_tracking_distance_label),
        formattedValue = PerformedValueFormatter.DistanceKm.formatRawValue(
            context,
            activityDetail.distance
        )
    )

enum class PerformedValueFormatter(val id: String, @StringRes val unitResId: Int) {
    DistanceKm("DistanceKm", R.string.performance_unit_distance_km) {
        override fun formatRawValue(context: Context, rawValue: Double): String =
            String.format(
                "%.2f ${getUnitName(context)}",
                PerformanceUnit.DistanceKm.fromRawValue(rawValue)
            )
    },
    PaceMinutePerKm("PaceMinutePerKm", R.string.performance_unit_pace_min_per_km) {
        override fun formatRawValue(context: Context, rawValue: Double): String {
            val minute = PerformanceUnit.PaceMinutePerKm.fromRawValue(rawValue)
            val intSecond = ((minute - minute.toInt()) * 60).toInt()
            val intMinute = minute.toInt()
            return "$intMinute:$intSecond ${getUnitName(context)}"
        }
    },
    SpeedKmPerHour("SpeedKmPerHour", R.string.performance_unit_speed) {
        override fun formatRawValue(context: Context, rawValue: Double): String =
            "${PerformanceUnit.SpeedKmPerHour.fromRawValue(rawValue)} ${getUnitName(context)}"
    },
    DurationHourMinuteSecond("DurationHourMinuteSecond", 0) {
        override fun formatRawValue(context: Context, rawValue: Double): String {
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
    }
    ;

    abstract fun formatRawValue(context: Context, rawValue: Double): String
    fun getUnitName(context: Context) = context.getString(unitResId)
}
