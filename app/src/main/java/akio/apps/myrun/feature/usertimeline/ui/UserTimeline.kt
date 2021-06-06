package akio.apps.myrun.feature.usertimeline.ui

import akio.apps.myrun.R
import akio.apps.myrun.feature.activitydetail.ActivityPerformedResultFormatter
import akio.apps.myrun.feature.activitydetail.ui.ActivityInfoHeaderView
import akio.apps.myrun.feature.usertimeline.UserTimelineViewModel
import akio.apps.myrun.feature.usertimeline.model.Activity
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.items
import com.google.accompanist.coil.rememberCoilPainter
import com.google.accompanist.flowlayout.FlowRow

@Composable
fun UserTimeline(
    contentPadding: PaddingValues,
    userTimelineViewModel: UserTimelineViewModel,
    onClickActivityAction: (Activity) -> Unit,
    onClickExportActivityFile: (Activity) -> Unit
) {
    val lazyPagingItems = userTimelineViewModel.myActivityList.collectAsLazyPagingItems()
    when {
        lazyPagingItems.loadState.refresh == LoadState.Loading ->
            FullscreenLoadingView()
        lazyPagingItems.loadState.append.endOfPaginationReached && lazyPagingItems.itemCount == 0 ->
            UserTimelineEmptyMessage()
        else -> UserTimelineActivityList(
            contentPadding,
            lazyPagingItems,
            userTimelineViewModel,
            onClickActivityAction,
            onClickExportActivityFile
        )
    }
}

@Composable
private fun UserTimelineActivityList(
    contentPadding: PaddingValues,
    lazyPagingItems: LazyPagingItems<Activity>,
    userTimelineViewModel: UserTimelineViewModel,
    onClickActivityAction: (Activity) -> Unit,
    onClickExportActivityFile: (Activity) -> Unit
) {
    val userRecentPlaceIdentifier by userTimelineViewModel.userRecentPlaceIdentifier
        .collectAsState(initial = null)
    LazyColumn(
        modifier = Modifier.fillMaxWidth().fillMaxHeight(),
        contentPadding = contentPadding
    ) {
        items(lazyPagingItems) { activity ->
            if (activity != null) {
                val activityDisplayPlaceName = userTimelineViewModel.getActivityDisplayPlaceName(
                    userRecentPlaceIdentifier,
                    activity.id,
                    activity.placeIdentifier
                )
                TimelineActivityItem(activity, activityDisplayPlaceName, onClickActivityAction) {
                    onClickExportActivityFile(activity)
                }
            } else {
                TimelineActivityPlaceholderItem()
            }
        }

        if (lazyPagingItems.loadState.append == LoadState.Loading) {
            item { LoadingItem() }
        }
    }
}

@Composable
private fun UserTimelineEmptyMessage() = Box(
    modifier = Modifier.fillMaxWidth().fillMaxHeight()
) {
    Text(
        text = stringResource(R.string.splash_welcome_text),
        modifier = Modifier
            .align(Alignment.BottomCenter)
            .padding(
                horizontal = dimensionResource(R.dimen.common_page_horizontal_padding),
                vertical = dimensionResource(R.dimen.user_timeline_listing_padding_bottom)
            ),
        color = colorResource(R.color.user_timeline_instruction_text),
        fontSize = 30.sp,
        fontStyle = FontStyle.Italic,
        fontWeight = FontWeight.Bold,
        textAlign = TextAlign.Center,
    )
}

@Composable
private fun FullscreenLoadingView() = Box(
    modifier = Modifier.fillMaxSize(),
    contentAlignment = Alignment.Center
) {
    LoadingItem()
}

@Composable
private fun LoadingItem() = Column(
    modifier = Modifier.padding(20.dp).fillMaxWidth(),
    horizontalAlignment = Alignment.CenterHorizontally
) {
    CircularProgressIndicator()
    Spacer(modifier = Modifier.height(10.dp))
    Text(
        text = stringResource(id = R.string.user_timeline_loading_item_message),
        color = colorResource(id = R.color.user_timeline_instruction_text),
        fontSize = 15.sp
    )
}

@Preview
@Composable
private fun LoadingItemPreview() = LoadingItem()

@Composable
fun TimelineActivityPlaceholderItem() = Surface(
    elevation = 2.dp,
    modifier = Modifier.fillMaxWidth().aspectRatio(1.5f)
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
    onClickActivityAction: (Activity) -> Unit,
    onClickExportFile: () -> Unit
) = Surface(
    elevation = 2.dp,
    modifier = Modifier
        .padding(top = 24.dp, bottom = 12.dp)
        .clickable { onClickActivityAction(activity) }
) {
    Column {
        ActivityInfoHeaderView(activity, activityDisplayPlaceName, onClickExportFile)
        Image(
            painter = rememberCoilPainter(
                request = activity.routeImage,
                shouldRefetchOnSizeChange = { _, _ -> false },
                previewPlaceholder = R.drawable.ic_run_circle
            ),
            contentDescription = "Activity route image",
            modifier = Modifier.fillMaxWidth().aspectRatio(1.5f),
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
