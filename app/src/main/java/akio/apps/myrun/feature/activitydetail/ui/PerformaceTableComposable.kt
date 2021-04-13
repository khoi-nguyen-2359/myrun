package akio.apps.myrun.feature.activitydetail.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp

@Composable
fun PerformanceTableComposable(vararg performedResults: PerformedResult) = Column {
    val iterator = performedResults.iterator()
    while (iterator.hasNext()) {
        Row(
            modifier = Modifier.fillMaxWidth()
        ) {
            PerformedResultCellComposable(iterator.next())
            if (iterator.hasNext()) {
                PerformedResultCellComposable(iterator.next())
            }
        }
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
    val valueAndUnitText = "${performedResult.value} ${performedResult.unit}"
    Text(
        text = valueAndUnitText,
        fontSize = 24.sp,
        textAlign = TextAlign.Center
    )
}

@Preview
@Composable
private fun PerformanceTableComposablePreview() {
    PerformanceTableComposable(
        PerformedResult("Distance", "3.2", "km"),
        PerformedResult("Moving Time", "31:49", ""),
        PerformedResult("HR", "151", "bpm"),
    )
}

data class PerformedResult(val label: String, val value: String, val unit: String)
