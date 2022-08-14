package akio.apps.myrun.feature.feed.ui

import akio.apps.myrun.data.activity.api.model.BaseActivityModel
import akio.apps.myrun.data.user.api.model.MeasureSystem
import akio.apps.myrun.feature.activity.R
import akio.apps.myrun.feature.core.ktx.px2dp
import akio.apps.myrun.feature.core.ktx.rememberViewModelProvider
import akio.apps.myrun.feature.core.navigation.HomeNavDestination
import akio.apps.myrun.feature.core.ui.AppColors
import akio.apps.myrun.feature.core.ui.AppDimensions
import akio.apps.myrun.feature.feed.ActivityFeedViewModel
import akio.apps.myrun.feature.feed.di.DaggerActivityFeedFeatureComponent
import akio.apps.myrun.feature.feed.model.FeedActivity
import akio.apps.myrun.feature.feed.model.FeedUiModel
import akio.apps.myrun.feature.feed.model.FeedUserFollowSuggestion
import akio.apps.myrun.feature.feed.ui.ActivityFeedColors.listBackground
import akio.apps.myrun.feature.feed.ui.ActivityFeedDimensions.activityItemVerticalMargin
import android.app.Application
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.items
import kotlin.math.roundToInt
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import timber.log.Timber

private object ActivityFeedColors {
    val listBackground: Color = Color.White
}

internal object ActivityFeedDimensions {
    val activityItemVerticalMargin: Dp = 12.dp
    val activityItemHorizontalPadding: Dp = 16.dp
    val feedItemVerticalPadding: Dp = 12.dp
    val feedItemHorizontalPadding: Dp = 12.dp
}

private const val REVEAL_ANIM_THRESHOLD = 10

@Composable
fun ActivityFeedScreen(
    navController: NavController,
    backStackEntry: NavBackStackEntry,
    contentPadding: PaddingValues,
    onClickExportActivityFile: (BaseActivityModel) -> Unit,
) {
    val application = LocalContext.current.applicationContext as Application
    val activityFeedViewModel = backStackEntry.rememberViewModelProvider { savedState ->
        DaggerActivityFeedFeatureComponent.factory().create(application, savedState).feedViewModel()
    }
    ActivityFeedScreen(
        activityFeedViewModel,
        contentPadding,
        onClickExportActivityFile,
        navController
    )
}

@Composable
private fun ActivityFeedScreen(
    activityFeedViewModel: ActivityFeedViewModel,
    contentPadding: PaddingValues,
    onClickExportActivityFile: (BaseActivityModel) -> Unit,
    navController: NavController,
) {
    Timber.d("Compose ActivityFeedScreen")
    val coroutineScope = rememberCoroutineScope()
    val systemBarsTopDp = WindowInsets.systemBars.getTop(LocalDensity.current).px2dp.dp
    val topBarHeightDp = AppDimensions.AppBarHeight + systemBarsTopDp
    val topBarOffsetY = remember { Animatable(0f) }
    val topBarHeightPx = with(LocalDensity.current) { topBarHeightDp.roundToPx().toFloat() }

    // insets may be updated => top bar height changes, so remember scroll connection with keys
    val nestedScrollConnection = remember(topBarHeightPx) {
        createTopBarAnimScrollConnection(topBarHeightPx, coroutineScope, topBarOffsetY)
    }

    val activityUploadBadge by activityFeedViewModel.activityUploadBadge.collectAsState(
        initial = ActivityFeedViewModel.ActivityUploadBadgeStatus.Hidden
    )

    val feedListState = rememberLazyListState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(nestedScrollConnection)
    ) {
        ActivityFeedContainer(
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
            Modifier
                .height(topBarHeightDp)
                .align(Alignment.TopCenter)
                .offset { IntOffset(x = 0, y = topBarOffsetY.value.roundToInt()) }
                .background(AppColors.primary),
            { onClickUploadCompleteBadge(coroutineScope, feedListState, activityFeedViewModel) }
        ) {
            navController.navigate(HomeNavDestination.UserPreferences.route)
        }
    }
}

private fun onClickUploadCompleteBadge(
    coroutineScope: CoroutineScope,
    feedListState: LazyListState,
    activityFeedViewModel: ActivityFeedViewModel,
) {
    coroutineScope.launch { feedListState.animateScrollToItem(0) }
    activityFeedViewModel.setUploadBadgeDismissed(true)
}

private fun createTopBarAnimScrollConnection(
    topBarHeightPx: Float,
    coroutineScope: CoroutineScope,
    topBarOffsetY: Animatable<Float, AnimationVector1D>,
) = object : NestedScrollConnection {
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

@Composable
private fun ActivityFeedContainer(
    activityFeedViewModel: ActivityFeedViewModel,
    contentPadding: PaddingValues,
    feedListState: LazyListState,
    onClickExportActivityFile: (BaseActivityModel) -> Unit,
    navController: NavController,
) {
    Timber.d("Compose ActivityFeedContainer")
    val lazyPagingItems = activityFeedViewModel.myActivityList.collectAsLazyPagingItems()
    val isLoadingInitialData by activityFeedViewModel.isInitialLoading.collectAsState(
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
    lazyPagingItems: LazyPagingItems<FeedUiModel>,
    onClickExportActivityFile: (BaseActivityModel) -> Unit,
    navController: NavController,
) {
    val userProfile by activityFeedViewModel.userProfile.collectAsState(initial = null)
    val preferredUnitSystem by activityFeedViewModel.preferredSystem.collectAsState(
        initial = MeasureSystem.Default
    )
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
            key = { feedItem -> feedItem.id }
        ) { feedItem ->
            when (feedItem) {
                is FeedActivity -> {
                    FeedActivityItem(
                        activityFeedViewModel,
                        feedItem.activityData,
                        userProfile,
                        preferredUnitSystem,
                        navController,
                        onClickExportActivityFile
                    )
                }
                is FeedUserFollowSuggestion -> {
                    FeedUserFollowSuggestionItem(feedItem)
                }
                null -> {
                    // do nothing
                }
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
        textAlign = TextAlign.Center
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

@Composable
internal fun FeedItem(content: @Composable () -> Unit) = Box(
    modifier = Modifier.padding(vertical = activityItemVerticalMargin)
) {
    Surface(
        elevation = 2.dp,
        content = content
    )
}

@Preview
@Composable
private fun LoadingItemPreview() = LoadingItem()
