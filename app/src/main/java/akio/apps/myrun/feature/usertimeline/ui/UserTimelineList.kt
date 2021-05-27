package akio.apps.myrun.feature.usertimeline.ui

import akio.apps.myrun.R
import akio.apps.myrun.feature.activitydetail.ActivityPerformedResultFormatter
import akio.apps.myrun.feature.activitydetail.ui.ActivityInfoHeaderComposable
import akio.apps.myrun.feature.usertimeline.UserTimelineViewModel
import akio.apps.myrun.feature.usertimeline.model.Activity
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.items
import com.google.accompanist.coil.rememberCoilPainter
import com.google.accompanist.flowlayout.FlowRow
import timber.log.Timber

@Composable
fun UserTimelineList(
    userTimelineViewModel: UserTimelineViewModel,
    onClickActivityAction: (Activity) -> Unit
) {
    val lazyPagingItems = userTimelineViewModel.myActivityList.collectAsLazyPagingItems()

    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
    ) {
        items(lazyPagingItems) { activity ->
            if (activity != null) {
                val activityDisplayPlaceName by produceState<String?>(
                    initialValue = null,
                    key1 = activity.id
                ) {
                    value = userTimelineViewModel.getActivityDisplayPlaceName(activity)
                }
                Timber.d("activyt!=null")
                TimelineActivityItem(activity, activityDisplayPlaceName, onClickActivityAction)
            } else {
                Timber.d("activyt==null")
                TimelineActivityPlaceholderItem()
            }
        }
    }
}

@Composable
fun TimelineActivityPlaceholderItem() = Surface(
    elevation = 2.dp,
    modifier = Modifier
        .fillMaxWidth()
        .aspectRatio(1.5f)
) {
    Image(
        painter = painterResource(id = R.drawable.common_avatar_placeholder_image),
        contentDescription = "Activity placeholder"
    )
}

@Composable
private fun TimelineActivityItem(
    activity: Activity,
    activityDisplayPlaceName: String?,
    onClickActivityAction: (Activity) -> Unit
) = Surface(
    elevation = 2.dp,
    modifier = Modifier
        .padding(top = 24.dp, bottom = 12.dp)
        .clickable { onClickActivityAction(activity) }
) {
    Column {
        ActivityInfoHeaderComposable(activity, activityDisplayPlaceName)
        Image(
            painter = rememberCoilPainter(
                request = activity.routeImage,
                shouldRefetchOnSizeChange = { _, _ -> false },
                previewPlaceholder = R.drawable.ic_run_circle
            ),
            contentDescription = "Activity route image",
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1.5f),
            contentScale = ContentScale.Crop,
        )
        TimelineActivityPerformanceRow(
            activity,
            listOf(
                ActivityPerformedResultFormatter.Distance,
                ActivityPerformedResultFormatter.Pace,
                ActivityPerformedResultFormatter.Duration
            )
        )
    }
}

@Composable
private fun TimelineActivityPerformanceRow(
    activity: Activity,
    performedResultFormatters: List<ActivityPerformedResultFormatter>
) = FlowRow(
    modifier = Modifier.padding(
        vertical = dimensionResource(id = R.dimen.common_item_vertical_padding)
    )
) {
    performedResultFormatters.forEach { performedResultFormatter ->
        PerformedResultItem(activity, performedResultFormatter)
    }
}

@Composable
private fun PerformedResultItem(
    activity: Activity,
    performedResultFormatter: ActivityPerformedResultFormatter
) = Column(
    modifier = Modifier.padding(
        horizontal = dimensionResource(id = R.dimen.common_item_horizontal_padding)
    )
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
        fontSize = 20.sp,
        textAlign = TextAlign.Center
    )
}
