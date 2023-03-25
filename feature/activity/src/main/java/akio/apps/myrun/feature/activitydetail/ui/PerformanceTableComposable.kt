package akio.apps.myrun.feature.activitydetail.ui

import akio.apps.myrun.data.activity.api.model.ActivityDataModel
import akio.apps.myrun.data.activity.api.model.ActivityType
import akio.apps.myrun.data.activity.api.model.AthleteInfo
import akio.apps.myrun.data.activity.api.model.BaseActivityModel
import akio.apps.myrun.data.activity.api.model.RunningActivityModel
import akio.apps.myrun.data.user.api.model.MeasureSystem
import akio.apps.myrun.feature.activity.R
import akio.apps.myrun.feature.core.measurement.TrackUnitFormatter
import akio.apps.myrun.feature.core.measurement.TrackUnitFormatterSet
import akio.apps.myrun.feature.core.measurement.UnitFormatterSetFactory
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
internal fun WidePerformanceTableComposable(
    activity: BaseActivityModel,
    trackUnitFormatterSet: TrackUnitFormatterSet,
    modifier: Modifier = Modifier,
) = Column(modifier) {
    val trackingValueFormatterList = remember(trackUnitFormatterSet) {
        createActivityFormatterList(activity, trackUnitFormatterSet)
    }

    FlexiblePerformanceTable(trackingValueFormatterList, chunkSize = 2, activity)
}

@Composable
private fun FlexiblePerformanceTable(
    trackingValueFormatterList: List<TrackUnitFormatter<*>>,
    chunkSize: Int,
    activity: BaseActivityModel,
    textHozAlignment: Alignment.Horizontal = Alignment.CenterHorizontally,
    statsFontSize: TextUnit = 20.sp,
) {
    trackingValueFormatterList.chunked(chunkSize).forEach { chunk ->
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    vertical = AppDimensions.rowVerticalPadding,
                    horizontal = AppDimensions.screenHorizontalPadding
                )
        ) {
            chunk.forEach { formatter ->
                PerformedResultCellComposable(activity, formatter, textHozAlignment, statsFontSize)
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

@Composable
internal fun CompactPerformanceTableComposable(
    activity: BaseActivityModel,
    trackUnitFormatterSet: TrackUnitFormatterSet,
    modifier: Modifier = Modifier,
) = Column(modifier) {
    val trackingValueFormatterList = remember(trackUnitFormatterSet) {
        createActivityFormatterList(activity, trackUnitFormatterSet)
    }
    FlexiblePerformanceTable(
        trackingValueFormatterList,
        chunkSize = 3,
        activity,
        textHozAlignment = Alignment.Start,
        statsFontSize = 18.sp
    )
}

private fun createActivityFormatterList(
    activity: BaseActivityModel,
    trackUnitFormatterSet: TrackUnitFormatterSet,
): List<TrackUnitFormatter<*>> =
    when (activity.activityType) {
        ActivityType.Running -> listOf(
            trackUnitFormatterSet.distanceFormatter,
            trackUnitFormatterSet.paceFormatter,
            trackUnitFormatterSet.durationFormatter
        )
        ActivityType.Cycling -> listOf(
            trackUnitFormatterSet.distanceFormatter,
            trackUnitFormatterSet.speedFormatter,
            trackUnitFormatterSet.durationFormatter
        )
        else -> emptyList()
    }

@Composable
private fun RowScope.PerformedResultCellComposable(
    activity: BaseActivityModel,
    trackUnitFormatter: TrackUnitFormatter<*>,
    hozAlignment: Alignment.Horizontal = Alignment.CenterHorizontally,
    statsFontSize: TextUnit = 20.sp,
) = Column(
    horizontalAlignment = hozAlignment,
    modifier = Modifier.weight(weight = 1f)
) {
    Text(
        text = trackUnitFormatter.getLabel(LocalContext.current),
        style = MaterialTheme.typography.caption,
        textAlign = TextAlign.Center,
        fontWeight = FontWeight.Bold
    )
    val formattedValue = trackUnitFormatter.getFormattedValue(activity)
    val unit = trackUnitFormatter.getUnit(LocalContext.current)
    Text(
        text = "$formattedValue $unit",
        fontSize = statsFontSize,
        textAlign = TextAlign.Center,
        fontWeight = FontWeight.Normal,
        style = MaterialTheme.typography.h6
    )
}

@Preview(showBackground = true, backgroundColor = 0xffffff)
@Composable
private fun Preview_WideTable() {
    WidePerformanceTableComposable(
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
        ),
        UnitFormatterSetFactory.createUnitFormatterSet(MeasureSystem.Default)
    )
}

@Preview(showBackground = true, backgroundColor = 0xffffff)
@Composable
private fun Preview_CompactTable() {
    CompactPerformanceTableComposable(
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
        ),
        UnitFormatterSetFactory.createUnitFormatterSet(MeasureSystem.Default)
    )
}
