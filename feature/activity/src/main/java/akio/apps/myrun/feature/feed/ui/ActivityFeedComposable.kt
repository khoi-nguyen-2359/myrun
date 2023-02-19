package akio.apps.myrun.feature.feed.ui

import akio.apps.myrun.data.activity.api.model.BaseActivityModel
import akio.apps.myrun.data.user.api.model.MeasureSystem
import akio.apps.myrun.data.user.api.model.UserFollowSuggestion
import akio.apps.myrun.feature.activity.R
import akio.apps.myrun.feature.core.ktx.px2dp
import akio.apps.myrun.feature.core.launchcatching.launchCatching
import akio.apps.myrun.feature.core.ui.AppColors
import akio.apps.myrun.feature.core.ui.AppDimensions
import akio.apps.myrun.feature.core.ui.ErrorDialog
import akio.apps.myrun.feature.feed.FeedViewModel
import akio.apps.myrun.feature.feed.di.DaggerFeedFeatureComponent
import akio.apps.myrun.feature.feed.model.FeedActivity
import akio.apps.myrun.feature.feed.model.FeedUiModel
import akio.apps.myrun.feature.feed.model.FeedUserFollowSuggestionList
import akio.apps.myrun.feature.feed.ui.ActivityFeedColors.listBackground
import akio.apps.myrun.feature.feed.ui.ActivityFeedDimensions.activityItemVerticalMargin
import android.app.Application
import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.graphics.Color
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
import androidx.navigation.NavController
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.items
import kotlin.math.roundToInt
import kotlinx.coroutines.CoroutineScope
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

@Composable
fun ActivityFeedComposable(
    appNavController: NavController,
    homeTabNavController: NavController,
    contentPaddingBottom: Dp,
    onClickExportActivityFile: (BaseActivityModel) -> Unit,
) {
    val uiState = rememberUiState(contentPaddingBottom)
    val navigator = rememberNavigator(appNavController, homeTabNavController)
    ActivityFeedComposableInternal(
        uiState,
        navigator,
        onClickExportActivityFile
    )
}

@Composable
private fun rememberViewModel(viewModelScope: CoroutineScope): FeedViewModel {
    val application = LocalContext.current.applicationContext as Application
    return remember {
        DaggerFeedFeatureComponent.factory().create(application, viewModelScope).feedViewModel()
    }
}

@Composable
private fun rememberUiState(contentPaddingBottom: Dp): FeedUiState {
    val coroutineScope = rememberCoroutineScope()
    val systemBarsTopDp = WindowInsets.systemBars.getTop(LocalDensity.current).px2dp.dp
    val topBarHeightDp = AppDimensions.AppBarHeight + systemBarsTopDp
    val topBarOffsetYAnimatable = remember { Animatable(0f) }
    val topBarHeightPx = with(LocalDensity.current) { topBarHeightDp.roundToPx().toFloat() }
    val feedListState = rememberLazyListState()
    return remember(topBarHeightDp) {
        FeedUiState(
            contentPaddingBottom,
            coroutineScope,
            topBarHeightDp,
            topBarHeightPx,
            topBarOffsetYAnimatable,
            feedListState,
        )
    }
}

@Composable
private fun rememberNavigator(
    appNavController: NavController,
    homeTabNavController: NavController,
): FeedNavigator = remember(appNavController, homeTabNavController) {
    FeedNavigator(appNavController, homeTabNavController)
}

@Composable
private fun ActivityFeedComposableInternal(
    uiState: FeedUiState,
    navigator: FeedNavigator,
    onClickExportActivityFile: (BaseActivityModel) -> Unit,
) {
    val feedViewModel = rememberViewModel(rememberCoroutineScope())
    Box(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(uiState.nestedScrollConnection)
    ) {
        ActivityFeedContainer(
            feedViewModel,
            uiState,
            navigator,
            onClickExportActivityFile
        )

        ActivityFeedTopBar(
            feedViewModel,
            Modifier
                .height(uiState.topBarHeightDp)
                .align(Alignment.TopCenter)
                .offset { IntOffset(x = 0, y = uiState.topBarOffsetYAnimatable.value.roundToInt()) }
                .background(AppColors.primary),
            onClickUserPreferencesButton = navigator::navigateUserPreferences,
            onDismissUploadBadge = uiState::dismissActivityUploadBadge
        )
    }

    uiState.popupErrorException?.let { popupError ->
        ErrorDialog(text = popupError.message ?: "") {
            uiState.popupErrorException = null
        }
    }
}

@Composable
private fun ActivityFeedContainer(
    feedViewModel: FeedViewModel,
    uiState: FeedUiState,
    navigator: FeedNavigator,
    onClickExportActivityFile: (BaseActivityModel) -> Unit,
) {
    val lazyPagingItems = feedViewModel.activityPagingFlow.collectAsLazyPagingItems()
    when {
        // lazyPagingItems.loadState.refresh == LoadState.Loading &&
        //     lazyPagingItems.itemCount == 0 -> {
        //     FullscreenLoadingView()
        // }
        lazyPagingItems.loadState.append.endOfPaginationReached &&
            lazyPagingItems.itemCount == 0 -> {
            ActivityFeedEmptyMessage(
                Modifier.padding(bottom = uiState.contentPaddings.calculateBottomPadding() + 8.dp)
            )
        }
        else -> ActivityFeedItemList(
            feedViewModel,
            uiState,
            navigator,
            lazyPagingItems,
            onClickExportActivityFile
        )
    }
}

@Composable
private fun ActivityFeedItemList(
    feedViewModel: FeedViewModel,
    uiState: FeedUiState,
    navigator: FeedNavigator,
    lazyPagingItems: LazyPagingItems<FeedUiModel>,
    onClickExportActivityFile: (BaseActivityModel) -> Unit,
) {
    Timber.d("render ActivityFeedItemList")
    val userProfile by feedViewModel.userProfileFlow.collectAsState(initial = null)
    val preferredUnitSystem by feedViewModel.measureSystem.collectAsState(
        initial = MeasureSystem.Default
    )
    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .background(listBackground),
        contentPadding = uiState.contentPaddings,
        state = uiState.feedListState
    ) {
        items(
            lazyPagingItems,
            key = { feedItem -> feedItem.id }
        ) { feedItem ->
            when (feedItem) {
                is FeedActivity -> {
                    FeedActivityItem(
                        feedItem,
                        userProfile,
                        preferredUnitSystem,
                        navigator,
                        onClickExportActivityFile
                    )
                }
                is FeedUserFollowSuggestionList -> {
                    FeedUserFollowSuggestionItem(
                        feedItem,
                        { followUser(uiState, feedViewModel, it) },
                        navigator::navigateNormalUserStats
                    )
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

private fun followUser(
    uiState: FeedUiState,
    viewModel: FeedViewModel,
    followSuggestion: UserFollowSuggestion,
) {
    uiState.uiScope.launchCatching({
        uiState.popupErrorException = it
    }) {
        viewModel.followUser(followSuggestion)
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
    Timber.d("render FullscreenLoadingView")
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
