package akio.apps.myrun.feature.activitydetail.ui

import akio.apps.myrun.R
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
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun PerformanceTableComposable(performedResults: List<PerformedResult>) = Column(
    Modifier.padding(5.dp)
) {
    val iterator = performedResults.iterator()
    while (iterator.hasNext()) {
        Row(modifier = Modifier.fillMaxWidth().padding(5.dp)) {
            PerformedResultCellComposable(iterator.next())
            if (iterator.hasNext()) {
                PerformedResultCellComposable(iterator.next())
            }
        }
        Divider(
            color = colorResource(id = R.color.activity_detail_performance_table_divider),
            modifier = Modifier.fillMaxWidth().height(0.5.dp)
        )
    }
}

@Composable
private fun RowScope.PerformedResultCellComposable(performedResult: PerformedResult) = Column(
    horizontalAlignment = Alignment.CenterHorizontally,
    modifier = Modifier.weight(weight = 1f)
) {
    Text(
        text = performedResult.label,
        fontSize = 10.sp,
        textAlign = TextAlign.Center
    )
    Text(
        text = performedResult.formattedValue,
        fontSize = 24.sp,
        textAlign = TextAlign.Center
    )
}

@Preview(showSystemUi = true)
@Composable
private fun PerformanceTableComposablePreview() {
    PerformanceTableComposable(
        listOf(
            PerformedResult("Distance", "3.2 km"),
            PerformedResult("Moving Time", "31:49"),
            PerformedResult("HR", "151 bpm")
        )
    )
}

data class PerformedResult(val label: String, val formattedValue: String)
