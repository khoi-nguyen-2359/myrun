package akio.apps.myrun.feature.activitydetail.ui

import akio.apps.myrun.data.activity.api.model.ActivityDataModel
import akio.apps.myrun.data.activity.api.model.ActivityType
import akio.apps.myrun.data.activity.api.model.AthleteInfo
import akio.apps.myrun.data.activity.api.model.BaseActivityModel
import akio.apps.myrun.data.activity.api.model.RunningActivityModel
import akio.apps.myrun.feature.TrackingValueFormatter
import akio.apps.myrun.feature.activity.R
import akio.apps.myrun.feature.core.ui.AppDimensions
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
internal fun PerformanceTableComposable(
    activity: BaseActivityModel,
    modifier: Modifier = Modifier,
) = Column(modifier) {
    val trackingValueFormatterList = createActivityFormatterList(activity)

    val iterator = trackingValueFormatterList.iterator()
    while (iterator.hasNext()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AppDimensions.rowVerticalPadding)
        ) {
            this.PerformedResultCellComposable(activity, iterator.next())
            if (iterator.hasNext()) {
                this.PerformedResultCellComposable(activity, iterator.next())
            }
        }
        Divider(
            color = colorResource(id = R.color.activity_detail_performance_table_divider),
            modifier = Modifier
                .fillMaxWidth()
                .height(0.5.dp)
        )
    }
}

private fun createActivityFormatterList(
    activity: BaseActivityModel,
): List<TrackingValueFormatter<*>> =
    when (activity.activityType) {
        ActivityType.Running -> listOf(
            TrackingValueFormatter.DistanceKm,
            TrackingValueFormatter.PaceMinutePerKm,
            TrackingValueFormatter.DurationHourMinuteSecond
        )
        ActivityType.Cycling -> listOf(
            TrackingValueFormatter.DistanceKm,
            TrackingValueFormatter.SpeedKmPerHour,
            TrackingValueFormatter.DurationHourMinuteSecond
        )
        else -> emptyList()
    }

@Composable
private fun RowScope.PerformedResultCellComposable(
    activity: BaseActivityModel,
    valueFormatter: TrackingValueFormatter<*>,
) = Column(
    horizontalAlignment = Alignment.CenterHorizontally,
    modifier = Modifier.weight(weight = 1f)
) {
    Text(
        text = valueFormatter.getLabel(LocalContext.current),
        style = MaterialTheme.typography.caption,
        textAlign = TextAlign.Center,
        fontWeight = FontWeight.Bold
    )
    val formattedValue = valueFormatter.getFormattedValue(activity)
    val unit = valueFormatter.getUnit(LocalContext.current)
    Text(
        text = "$formattedValue $unit",
        style = MaterialTheme.typography.h6,
        textAlign = TextAlign.Center,
        fontWeight = FontWeight.Normal
    )
}

@Preview(showBackground = true, backgroundColor = 0xffffff)
@Composable
private fun PreviewTable() {
    PerformanceTableComposable(
        activity = RunningActivityModel(
            activityData = ActivityDataModel(
                id = "id",
                activityType = ActivityType.Running,
                name = "Evening Run",
                routeImage = "http://example.com",
                placeIdentifier = null,
                startTime = System.currentTimeMillis(),
                endTime = 2000L,
                duration = 1000L,
                distance = 100.0,
                encodedPolyline = "",
                athleteInfo = AthleteInfo(
                    userId = "id",
                    userName = "Khoi Nguyen",
                    userAvatar = "userAvatar"
                )
            ),
            pace = 1.0,
            cadence = 160
        )
    )
}
