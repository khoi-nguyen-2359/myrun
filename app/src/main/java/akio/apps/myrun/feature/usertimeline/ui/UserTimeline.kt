package akio.apps.myrun.feature.usertimeline.ui

import akio.apps.myrun.R
import akio.apps.myrun.data.activity.model.ActivityType
import akio.apps.myrun.feature.activitydetail.TrackingValueFormatter
import akio.apps.myrun.feature.activitydetail.ui.ActivityInfoHeaderView
import akio.apps.myrun.feature.activitydetail.ui.ActivityRouteImage
import akio.apps.myrun.feature.usertimeline.UserTimelineViewModel
import akio.apps.myrun.feature.usertimeline.model.Activity
import akio.apps.myrun.feature.usertimeline.model.ActivityData
import akio.apps.myrun.feature.usertimeline.model.RunningActivity
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.items
import timber.log.Timber

@Composable
fun UserTimeline(
    userTimelineViewModel: UserTimelineViewModel,
    contentPadding: PaddingValues,
    onClickActivityAction: (Activity) -> Unit,
    onClickExportActivityFile: (Activity) -> Unit,
    onClickUserAvatar: (String) -> Unit
) {
    val lazyPagingItems = userTimelineViewModel.myActivityList.collectAsLazyPagingItems()
    val isLoadingInitialData by userTimelineViewModel.isLoadingInitialData
        .collectAsState(initial = true)
    when {
        isLoadingInitialData ||
            lazyPagingItems.loadState.refresh == LoadState.Loading &&
            lazyPagingItems.itemCount == 0 -> FullscreenLoadingView()
        lazyPagingItems.loadState.append.endOfPaginationReached &&
            lazyPagingItems.itemCount == 0 -> UserTimelineEmptyMessage()
        else -> UserTimelineActivityList(
            userTimelineViewModel,
            contentPadding,
            lazyPagingItems,
            onClickActivityAction,
            onClickExportActivityFile,
            onClickUserAvatar
        )
    }
}

@Suppress("UNUSED_PARAMETER")
@Composable
private fun UserTimelineActivityList(
    userTimelineViewModel: UserTimelineViewModel,
    contentPadding: PaddingValues,
    lazyPagingItems: LazyPagingItems<Activity>,
    onClickActivityAction: (Activity) -> Unit,
    onClickExportActivityFile: (Activity) -> Unit,
    onClickUserAvatar: (String) -> Unit
) {
    Timber.d("render UserTimelineActivityList pagingItems=$lazyPagingItems")
    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(),
        contentPadding = contentPadding
    ) {
        items(lazyPagingItems) { activity ->
            if (activity != null) {
                val activityDisplayPlaceName = remember {
                    userTimelineViewModel.getActivityDisplayPlaceName(activity)
                }
                TimelineActivityItem(
                    activity, activityDisplayPlaceName, onClickActivityAction,
                    { onClickExportActivityFile(activity) },
                    { onClickUserAvatar(activity.athleteInfo.userId) }
                )
            }
        }

        if (lazyPagingItems.loadState.append == LoadState.Loading) {
            item { LoadingItem() }
        }
    }
}

@Composable
private fun UserTimelineEmptyMessage() = Box(
    modifier = Modifier
        .fillMaxWidth()
        .fillMaxHeight()
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
    modifier = Modifier
        .padding(20.dp)
        .fillMaxWidth(),
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
private fun TimelineActivityItem(
    activity: Activity,
    activityDisplayPlaceName: String,
    onClickActivityAction: (Activity) -> Unit,
    onClickExportFile: () -> Unit,
    onClickUserAvatar: () -> Unit
) = Surface(
    elevation = 2.dp,
    modifier = Modifier.padding(top = 24.dp, bottom = 12.dp)
) {
    Timber.d(
        "render TimelineActivityItem " +
            "activity=${activity.id}" +
            " placeName=$activityDisplayPlaceName"
    )
    Column(modifier = Modifier.clickable { onClickActivityAction(activity) }) {
        ActivityInfoHeaderView(
            activity,
            activityDisplayPlaceName,
            onClickExportFile,
            onClickUserAvatar,
            isShareMenuVisible = false
        )
        ActivityRouteImage(activity)
        TimelineActivityPerformanceRow(activity)
    }
}

private fun createActivityFormatterList(activityType: ActivityType): List<TrackingValueFormatter> =
    when (activityType) {
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
private fun TimelineActivityPerformanceRow(activity: Activity) {
    val valueFormatterList = remember { createActivityFormatterList(activity.activityType) }
    Row(
        modifier = Modifier
            .padding(vertical = dimensionResource(id = R.dimen.common_item_vertical_padding))
            .fillMaxWidth()
    ) {
        valueFormatterList.forEach { performedResultFormatter ->
            PerformedResultItem(activity, performedResultFormatter)
        }
    }
}

@Composable
private fun PerformedResultItem(
    activity: Activity,
    performedResultFormatter: TrackingValueFormatter
) = Column(
    modifier = Modifier.padding(
        horizontal = dimensionResource(id = R.dimen.common_item_horizontal_padding)
    )
) {
    val context = LocalContext.current
    val label = remember { performedResultFormatter.getLabel(context) }
    Text(
        text = label,
        fontSize = 10.sp,
        textAlign = TextAlign.Center
    )
    val formattedValue = remember { performedResultFormatter.getFormattedValue(activity) }
    val unit = remember { performedResultFormatter.getUnit(context) }
    Text(
        text = "$formattedValue $unit",
        fontSize = 20.sp,
        textAlign = TextAlign.Center
    )
}

@Preview
@Composable
private fun PreviewTimelineActivityItem() {
    TimelineActivityItem(
        activity = RunningActivity(
            activityData = ActivityData(
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
                athleteInfo = Activity.AthleteInfo(
                    userId = "id",
                    userName = "Khoi Nguyen",
                    userAvatar = "userAvatar"
                )
            ),
            pace = 1.0,
            cadence = 160
        ),
        activityDisplayPlaceName = "activityDisplayPlaceName",
        onClickActivityAction = { },
        onClickExportFile = { },
        onClickUserAvatar = { }
    )
}
