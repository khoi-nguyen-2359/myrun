package akio.apps.myrun.feature.feed.ui

import akio.apps.common.feature.ui.px2dp
import akio.apps.myrun.R
import akio.apps.myrun.data.activity.api.model.ActivityDataModel
import akio.apps.myrun.data.activity.api.model.ActivityModel
import akio.apps.myrun.data.activity.api.model.ActivityType
import akio.apps.myrun.data.activity.api.model.RunningActivityModel
import akio.apps.myrun.feature.activitydetail.ActivityDateTimeFormatter
import akio.apps.myrun.feature.activitydetail.TrackingValueFormatter
import akio.apps.myrun.feature.activitydetail.ui.ActivityRouteImage
import akio.apps.myrun.feature.base.navigation.HomeNavDestination
import akio.apps.myrun.feature.base.ui.AppColors
import akio.apps.myrun.feature.feed.impl.ActivityFeedViewModel
import akio.apps.myrun.feature.feed.ui.FeedColors.listBackground
import akio.apps.myrun.feature.feed.ui.FeedDimensions.activityItemHorizontalMargin
import akio.apps.myrun.feature.feed.ui.FeedDimensions.activityItemHorizontalPadding
import akio.apps.myrun.feature.feed.ui.FeedDimensions.activityItemVerticalMargin
import akio.apps.myrun.feature.feed.ui.FeedDimensions.activityItemVerticalPadding
import akio.apps.myrun.feature.home.ui.HomeScreenDimensions
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.ButtonDefaults.elevation
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedButton
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Share
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
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
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.items
import coil.compose.rememberImagePainter
import com.google.accompanist.insets.LocalWindowInsets
import kotlin.math.roundToInt
import kotlinx.coroutines.launch
import timber.log.Timber

private object FeedColors {
    val listBackground: Color = Color.White
}

private object FeedDimensions {
    val activityItemHorizontalMargin: Dp = 0.dp
    val activityItemVerticalMargin: Dp = 12.dp
    val activityItemHorizontalPadding: Dp = 16.dp
    val activityItemVerticalPadding: Dp = 12.dp
}

private const val REVEAL_ANIM_THRESHOLD = 10

@Composable
fun ActivityFeed(
    activityFeedViewModel: ActivityFeedViewModel,
    contentPadding: PaddingValues,
    onClickExportActivityFile: (ActivityModel) -> Unit,
    navController: NavController,
) {
    val coroutineScope = rememberCoroutineScope()
    val insets = LocalWindowInsets.current
    val topBarHeightDp = HomeScreenDimensions.AppBarHeight + insets.systemBars.top.px2dp.dp
    val topBarOffsetY = remember { Animatable(0f) }
    val topBarHeightPx = with(LocalDensity.current) { topBarHeightDp.roundToPx().toFloat() }

    // insets may be updated => top bar height changes, so remember scroll connection with keys
    val nestedScrollConnection = remember(topBarHeightPx) {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                val delta = available.y
                val targetOffset = when {
                    delta >= REVEAL_ANIM_THRESHOLD -> 0f
                    delta <= -REVEAL_ANIM_THRESHOLD -> -topBarHeightPx
                    else -> return Offset.Zero
                }
                coroutineScope.launch {
                    topBarOffsetY.animateTo(targetOffset)
                }

                return Offset.Zero
            }
        }
    }

    val activityUploadBadge by activityFeedViewModel.activityUploadBadge.collectAsState(
        initial = null
    )

    val feedListState = rememberLazyListState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(nestedScrollConnection)
    ) {
        ActivityFeed(
            activityFeedViewModel = activityFeedViewModel,
            contentPadding = contentPadding.clone(
                top = contentPadding.calculateTopPadding() + topBarHeightDp
            ),
            feedListState = feedListState,
            onClickExportActivityFile = onClickExportActivityFile,
            navController = navController
        )

        ActivityFeedTopBar(
            activityUploadBadge,
            { coroutineScope.launch { feedListState.animateScrollToItem(0) } },
            Modifier
                .height(topBarHeightDp)
                .align(Alignment.TopCenter)
                .offset { IntOffset(x = 0, y = topBarOffsetY.value.roundToInt()) }
                .background(AppColors.primary)
        )
    }
}

private fun PaddingValues.clone(
    start: Dp = calculateStartPadding(LayoutDirection.Ltr),
    top: Dp = calculateTopPadding(),
    end: Dp = calculateEndPadding(LayoutDirection.Ltr),
    bottom: Dp = calculateBottomPadding(),
): PaddingValues = PaddingValues(start, top, end, bottom)

@Composable
fun ActivityFeed(
    activityFeedViewModel: ActivityFeedViewModel,
    contentPadding: PaddingValues,
    feedListState: LazyListState,
    onClickExportActivityFile: (ActivityModel) -> Unit,
    navController: NavController,
) {
    val lazyPagingItems = activityFeedViewModel.myActivityList.collectAsLazyPagingItems()
    Timber.d("feed source load state ${lazyPagingItems.loadState.source}")
    val isLoadingInitialData by activityFeedViewModel.isLoadingInitialData.collectAsState(
        initial = false
    )
    when {
        isLoadingInitialData ||
            (
                lazyPagingItems.loadState.refresh == LoadState.Loading &&
                    lazyPagingItems.itemCount == 0
                ) -> {
            FullscreenLoadingView()
        }
        lazyPagingItems.loadState.append.endOfPaginationReached &&
            lazyPagingItems.itemCount == 0 -> {
            ActivityFeedEmptyMessage(
                Modifier.padding(bottom = contentPadding.calculateBottomPadding() + 8.dp)
            )
        }
        else -> ActivityFeedItemList(
            activityFeedViewModel,
            contentPadding,
            feedListState,
            lazyPagingItems,
            onClickExportActivityFile,
            navController
        )
    }
}

@Composable
private fun ActivityFeedItemList(
    activityFeedViewModel: ActivityFeedViewModel,
    contentPadding: PaddingValues,
    feedListState: LazyListState,
    lazyPagingItems: LazyPagingItems<ActivityModel>,
    onClickExportActivityFile: (ActivityModel) -> Unit,
    navController: NavController,
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
                    activityFeedViewModel.getActivityDisplayPlaceName(activity)
                }
                FeedActivityItem(
                    activity, activityDisplayPlaceName,
                    { activityModel ->
                        val route = HomeNavDestination.ActivityDetail.routeWithActivityId(
                            activityModel.id
                        )
                        navController.navigate(route)
                    },
                    { onClickExportActivityFile(activity) },
                    {
                        val route = HomeNavDestination.Profile.routeWithUserId(
                            activity.athleteInfo.userId
                        )
                        navController.navigate(route)
                    }
                )
            }
        }

        if (lazyPagingItems.loadState.append == LoadState.Loading) {
            item { LoadingItem() }
        }
    }
}

@Composable
private fun ActivityFeedEmptyMessage(modifier: Modifier = Modifier) = Box(
    modifier = modifier
        .fillMaxWidth()
        .fillMaxHeight()
) {
    Text(
        text = stringResource(R.string.splash_welcome_text),
        modifier = Modifier
            .align(Alignment.BottomCenter)
            .padding(horizontal = dimensionResource(R.dimen.common_page_horizontal_padding)),
        color = colorResource(R.color.activity_feed_instruction_text),
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
        text = stringResource(id = R.string.activity_feed_loading_item_message),
        color = colorResource(id = R.color.activity_feed_instruction_text),
        fontSize = 15.sp
    )
}

@Preview
@Composable
private fun LoadingItemPreview() = LoadingItem()

@Composable
private fun FeedActivityItem(
    activity: ActivityModel,
    activityDisplayPlaceName: String,
    onClickActivityAction: (ActivityModel) -> Unit,
    onClickExportFile: () -> Unit,
    onClickUserAvatar: () -> Unit,
) = FeedItem {
    Column(
        modifier = Modifier.clickable {
            onClickActivityAction(activity)
        }
    ) {
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
private fun ActivityRouteImageBox(activity: ActivityModel) =
    Box(contentAlignment = Alignment.TopStart) {
        ActivityRouteImage(activity)
        ActivityPerformanceRow(
            activity,
            modifier = Modifier.padding(
                horizontal = activityItemHorizontalPadding,
                vertical = activityItemVerticalPadding
            )
        )
    }

private fun createActivityFormatterList(
    activityType: ActivityType,
): List<TrackingValueFormatter<*>> =
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
private fun ActivityPerformanceRow(activity: ActivityModel, modifier: Modifier = Modifier) {
    var isExpanded by rememberSaveable { mutableStateOf(false) }
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

    OutlinedButton(
        shape = RoundedCornerShape(3.dp),
        onClick = { isExpanded = !isExpanded },
        border = null,
        colors = ButtonDefaults.outlinedButtonColors(
            backgroundColor = Color(0xff494949),
            contentColor = Color.White
        ),
        modifier = modifier.height(30.dp),
        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
        elevation = elevation(defaultElevation = 4.dp)
    ) {
        Text(
            text = performanceValue,
            style = MaterialTheme.typography.caption,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.animateContentSize()
        )
    }
}

@Preview
@Composable
private fun PreviewFeedActivityItem() {
    FeedActivityItem(
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
                distance = 1234.0,
                encodedPolyline = "",
                athleteInfo = ActivityModel.AthleteInfo(
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
    activity: ActivityModel,
    activityDisplayPlaceName: String?,
    onClickExportFile: () -> Unit,
    onClickUserAvatar: () -> Unit,
    isShareMenuVisible: Boolean = true,
) = Column(modifier = Modifier.padding(start = activityItemHorizontalPadding)) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        UserAvatarImage(activityDetail = activity, onClickUserAvatar)
        Spacer(modifier = Modifier.size(12.dp))
        Column(modifier = Modifier.weight(1.0f)) {
            AthleteNameText(activity)
            Spacer(modifier = Modifier.height(2.dp))
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
private fun AthleteNameText(activityDetail: ActivityModel) = Text(
    text = activityDetail.athleteInfo.userName.orEmpty(),
    maxLines = 1,
    overflow = TextOverflow.Ellipsis,
    fontWeight = FontWeight.Bold,
    style = MaterialTheme.typography.subtitle1
)

@Composable
private fun ActivityNameText(
    activityDetail: ActivityModel,
    modifier: Modifier = Modifier,
) = Text(
    text = activityDetail.name,
    modifier = modifier.fillMaxWidth(),
    fontWeight = FontWeight.Bold,
    style = MaterialTheme.typography.h6
)

@Composable
private fun ActivityShareMenu(
    onClickExportFile: () -> Unit,
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
private fun ActivityTimeAndPlaceText(
    activityDetail: ActivityModel,
    activityDisplayPlaceName: String?,
) {
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
        style = MaterialTheme.typography.caption
    )
}

@Composable
private fun UserAvatarImage(
    activityDetail: ActivityModel,
    onClickUserAvatar: () -> Unit,
) {
    val avatarDimension = 46.dp
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
private fun FeedItem(content: @Composable () -> Unit) = Box(
    modifier = Modifier.padding(
        horizontal = activityItemHorizontalMargin,
        vertical = activityItemVerticalMargin
    )
) {
    Surface(
        elevation = 2.dp,
        content = content
    )
}
