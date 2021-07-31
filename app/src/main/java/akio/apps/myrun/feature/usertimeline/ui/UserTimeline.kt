package akio.apps.myrun.feature.usertimeline.ui

import akio.apps.myrun.R
import akio.apps.myrun.data.activity.model.ActivityType
import akio.apps.myrun.feature.activitydetail.ActivityDateTimeFormatter
import akio.apps.myrun.feature.activitydetail.TrackingValueFormatter
import akio.apps.myrun.feature.activitydetail.ui.ActivityRouteImage
import akio.apps.myrun.feature.usertimeline.UserTimelineViewModel
import akio.apps.myrun.feature.usertimeline.model.Activity
import akio.apps.myrun.feature.usertimeline.model.ActivityData
import akio.apps.myrun.feature.usertimeline.model.RunningActivity
import akio.apps.myrun.feature.usertimeline.ui.TimelineColors.listBackground
import akio.apps.myrun.feature.usertimeline.ui.TimelineDimensions.activityItemHorizontalMargin
import akio.apps.myrun.feature.usertimeline.ui.TimelineDimensions.activityItemHorizontalPadding
import akio.apps.myrun.feature.usertimeline.ui.TimelineDimensions.activityItemVerticalMargin
import akio.apps.myrun.feature.usertimeline.ui.TimelineDimensions.activityItemVerticalPadding
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Share
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.items
import coil.compose.rememberImagePainter
import timber.log.Timber

private object TimelineColors {
    val listBackground: Color = Color.White
}

private object TimelineDimensions {
    val timelineItemCornerRadius: Dp = 6.dp
    val activityItemHorizontalMargin: Dp = 0.dp
    val activityItemVerticalMargin: Dp = 12.dp
    val activityItemHorizontalPadding: Dp = 16.dp
    val activityItemVerticalPadding: Dp = 12.dp
}

@Composable
fun UserTimeline(
    userTimelineViewModel: UserTimelineViewModel,
    contentPadding: PaddingValues,
    feedListState: LazyListState,
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
            lazyPagingItems.itemCount == 0 -> UserTimelineEmptyMessage(
            Modifier.padding(bottom = contentPadding.calculateBottomPadding() + 8.dp)
        )
        else -> UserTimelineActivityList(
            userTimelineViewModel,
            contentPadding,
            feedListState,
            lazyPagingItems,
            onClickActivityAction,
            onClickExportActivityFile,
            onClickUserAvatar
        )
    }
}

@Composable
private fun UserTimelineActivityList(
    userTimelineViewModel: UserTimelineViewModel,
    contentPadding: PaddingValues,
    feedListState: LazyListState,
    lazyPagingItems: LazyPagingItems<Activity>,
    onClickActivityAction: (Activity) -> Unit,
    onClickExportActivityFile: (Activity) -> Unit,
    onClickUserAvatar: (String) -> Unit
) {
    Timber.d("render UserTimelineActivityList pagingItems=$lazyPagingItems")
    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .background(listBackground),
        contentPadding = contentPadding,
        state = feedListState
    ) {
        items(
            lazyPagingItems,
            key = { activity -> activity.id }
        ) { activity ->
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
private fun UserTimelineEmptyMessage(modifier: Modifier = Modifier) = Box(
    modifier = modifier
        .fillMaxWidth()
        .fillMaxHeight()
) {
    Text(
        text = stringResource(R.string.splash_welcome_text),
        modifier = Modifier
            .align(Alignment.BottomCenter)
            .padding(horizontal = dimensionResource(R.dimen.common_page_horizontal_padding)),
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
) = TimelineItem {
    Column(modifier = Modifier.clickable { onClickActivityAction(activity) }) {
        Spacer(modifier = Modifier.height(activityItemVerticalPadding))
        ActivityInformationView(
            activity,
            activityDisplayPlaceName,
            onClickExportFile,
            onClickUserAvatar,
            isShareMenuVisible = true
        )
        Spacer(modifier = Modifier.height(8.dp))
        ActivityRouteImageBox(activity)
    }
}

@Composable
private fun ActivityRouteImageBox(activity: Activity) =
    Box(contentAlignment = Alignment.TopStart) {
        ActivityRouteImage(activity)
        TimelineActivityPerformanceRow(
            activity,
            modifier = Modifier.padding(
                horizontal = activityItemHorizontalPadding,
                vertical = activityItemVerticalPadding
            )
        )
    }

private fun createActivityFormatterList(activityType: ActivityType): List<TrackingValueFormatter> =
    when (activityType) {
        ActivityType.Running -> listOf(
            TrackingValueFormatter.DistanceKm,
            TrackingValueFormatter.PaceMinutePerKm
        )
        ActivityType.Cycling -> listOf(
            TrackingValueFormatter.DistanceKm,
            TrackingValueFormatter.SpeedKmPerHour
        )
        else -> emptyList()
    }

private const val PERFORMANCE_VALUE_DELIM = " - "

@Composable
private fun TimelineActivityPerformanceRow(activity: Activity, modifier: Modifier = Modifier) {
    var isExpanded by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val valueFormatterList = remember { createActivityFormatterList(activity.activityType) }
    val performanceValue = remember(isExpanded) {
        valueFormatterList.foldIndexed("") { index, acc, performedResultFormatter ->
            val formattedValue = performedResultFormatter.getFormattedValue(activity)
            val unit = performedResultFormatter.getUnit(context)
            val presentedText = "$formattedValue $unit"
            if (!isExpanded && index == 0) {
                return@remember presentedText
            }
            "$acc$presentedText$PERFORMANCE_VALUE_DELIM"
        }
            .removeSuffix(PERFORMANCE_VALUE_DELIM)
    }
    Surface(
        elevation = 4.dp,
        shape = RoundedCornerShape(4.dp),
        modifier = modifier.clickable { isExpanded = !isExpanded }
    ) {

        Text(
            modifier = Modifier
                .background(Color(0xff494949))
                .padding(horizontal = 8.dp, vertical = 6.dp)
                .animateContentSize(),
            color = Color.White,
            fontWeight = FontWeight.Bold,
            text = performanceValue,
            fontSize = 14.sp,
            textAlign = TextAlign.Center
        )
    }
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
                distance = 1234.0,
                encodedPolyline = "",
                athleteInfo = Activity.AthleteInfo(
                    userId = "id",
                    userName = "Khoi Nguyen",
                    userAvatar = "userAvatar"
                )
            ),
            pace = 12.34,
            cadence = 160
        ),
        activityDisplayPlaceName = "activityDisplayPlaceName",
        onClickActivityAction = { },
        onClickExportFile = { },
        onClickUserAvatar = { }
    )
}

@Composable
private fun ActivityInformationView(
    activity: Activity,
    activityDisplayPlaceName: String?,
    onClickExportFile: () -> Unit,
    onClickUserAvatar: () -> Unit,
    isShareMenuVisible: Boolean = true
) = Column(modifier = Modifier.padding(start = activityItemHorizontalPadding)) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        UserAvatarImage(activityDetail = activity, onClickUserAvatar)
        Spacer(modifier = Modifier.size(12.dp))
        Column(modifier = Modifier.weight(1.0f)) {
            AthleteNameText(activity)
            ActivityTimeAndPlaceText(activity, activityDisplayPlaceName)
        }
        if (isShareMenuVisible) {
            ActivityShareMenu(onClickExportFile)
        }
    }
    Spacer(modifier = Modifier.size(12.dp))
    ActivityNameText(activity)
}

@Composable
private fun AthleteNameText(activityDetail: Activity) = Text(
    text = activityDetail.athleteInfo.userName.orEmpty(),
    maxLines = 1,
    overflow = TextOverflow.Ellipsis,
    fontWeight = FontWeight.Bold,
    fontSize = 16.sp
)

@Composable
private fun ActivityNameText(
    activityDetail: Activity,
    modifier: Modifier = Modifier
) = Text(
    text = activityDetail.name,
    modifier = modifier.fillMaxWidth(),
    fontSize = 22.sp,
    fontWeight = FontWeight.Bold,
    letterSpacing = 0.5.sp
)

@Composable
private fun ActivityShareMenu(
    onClickExportFile: () -> Unit
) = Box(
    modifier = Modifier.padding(horizontal = 6.dp)
) {
    var isExpanded by remember { mutableStateOf(false) }
    IconButton(
        onClick = { isExpanded = !isExpanded }
    ) {
        Icon(
            imageVector = Icons.Outlined.Share,
            contentDescription = "Share icon"
        )
    }
    DropdownMenu(
        expanded = isExpanded,
        onDismissRequest = { isExpanded = false }
    ) {
        DropdownMenuItem(
            onClick = {
                onClickExportFile()
                isExpanded = false
            }
        ) {
            Text(text = stringResource(id = R.string.activity_details_share_menu_item_export_file))
        }
    }
}

@Composable
private fun ActivityTimeAndPlaceText(activityDetail: Activity, activityDisplayPlaceName: String?) {
    val activityDateTimeFormatter = remember(::ActivityDateTimeFormatter)
    val activityFormattedStartTime =
        remember { activityDateTimeFormatter.formatActivityDateTime(activityDetail.startTime) }
    val context = LocalContext.current
    val startTimeText = remember {
        Timber.d("making startTimeText")
        when (activityFormattedStartTime) {
            is ActivityDateTimeFormatter.Result.WithinToday -> context.getString(
                R.string.item_activity_time_today,
                activityFormattedStartTime.formattedValue
            )
            is ActivityDateTimeFormatter.Result.WithinYesterday -> context.getString(
                R.string.item_activity_time_yesterday,
                activityFormattedStartTime.formattedValue
            )
            is ActivityDateTimeFormatter.Result.FullDateTime ->
                activityFormattedStartTime.formattedValue
        }
    }
    Timber.d("startTimeText=$startTimeText")
    val timeAndPlaceText = remember {
        if (activityDisplayPlaceName.isNullOrEmpty()) {
            startTimeText
        } else {
            "$startTimeText \u00b7 $activityDisplayPlaceName"
        }
    }
    Text(
        text = timeAndPlaceText,
        overflow = TextOverflow.Ellipsis,
        maxLines = 2,
        fontSize = 13.sp
    )
}

@Composable
private fun UserAvatarImage(
    activityDetail: Activity,
    onClickUserAvatar: () -> Unit
) {
    val avatarDimension = 50.dp
    val avatarSize = with(LocalDensity.current) { avatarDimension.toPx() }
    Image(
        painter = rememberImagePainter(
            data = activityDetail.athleteInfo.userAvatar.orEmpty(),
            builder = {
                size(avatarSize.toInt())
                    .placeholder(R.drawable.common_avatar_placeholder_image)
                    .error(R.drawable.common_avatar_placeholder_image)
            }
        ),
        contentDescription = "Athlete avatar",
        modifier = Modifier
            .size(avatarDimension)
            .clip(CircleShape)
            .clickable { onClickUserAvatar() }
    )
}

@Composable
private fun TimelineItem(content: @Composable () -> Unit) = Box(
    modifier = Modifier.padding(
        horizontal = activityItemHorizontalMargin,
        vertical = activityItemVerticalMargin
    )
) {
    Surface(
        elevation = 2.dp,
//        shape = RoundedCornerShape(timelineItemCornerRadius),
        content = content
    )
}
