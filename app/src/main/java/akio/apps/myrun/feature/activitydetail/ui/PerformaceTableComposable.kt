package akio.apps.myrun.feature.activitydetail.ui

import akio.apps.myrun.R
import akio.apps.myrun.data.activity.model.ActivityType
import akio.apps.myrun.feature.activitydetail.ActivityPerformedResultFormatter
import akio.apps.myrun.feature.usertimeline.model.Activity
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Divider
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun PerformanceTableComposable(activity: Activity) = Column(Modifier.padding(5.dp)) {
    val performedResultFormatters = when (activity.activityType) {
        ActivityType.Running -> listOf(
            ActivityPerformedResultFormatter.Distance,
            ActivityPerformedResultFormatter.Pace,
            ActivityPerformedResultFormatter.Duration
        )
        ActivityType.Cycling -> listOf(
            ActivityPerformedResultFormatter.Distance,
            ActivityPerformedResultFormatter.Speed,
            ActivityPerformedResultFormatter.Duration
        )
        else -> emptyList()
    }

    val iterator = performedResultFormatters.iterator()
    while (iterator.hasNext()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(5.dp)
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

@Composable
private fun RowScope.PerformedResultCellComposable(
    activity: Activity,
    performedResultFormatter: ActivityPerformedResultFormatter
) = Column(
    horizontalAlignment = Alignment.CenterHorizontally,
    modifier = Modifier.weight(weight = 1f)
) {
    Text(
        text = performedResultFormatter.getLabel(LocalContext.current),
        fontSize = 10.sp,
        textAlign = TextAlign.Center
    )
    val formattedValue = performedResultFormatter.getFormattedPerformedResultValue(activity)
    val unit = performedResultFormatter.getUnit(LocalContext.current)
    Text(
        text = "$formattedValue $unit",
        fontSize = 24.sp,
        textAlign = TextAlign.Center
    )
}
