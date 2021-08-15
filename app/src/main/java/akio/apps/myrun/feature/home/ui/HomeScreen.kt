package akio.apps.myrun.feature.home.ui

import akio.apps.common.feature.ui.px2dp
import akio.apps.myrun.R
import akio.apps.myrun.feature.base.ui.AppBarIconButton
import akio.apps.myrun.feature.base.ui.AppColors
import akio.apps.myrun.feature.base.ui.AppDimensions
import akio.apps.myrun.feature.base.ui.AppTheme
import akio.apps.myrun.feature.home.ui.HomeScreenColors.uploadingBadgeContentColor
import akio.apps.myrun.feature.home.ui.HomeScreenDimensions.AppBarHeight
import akio.apps.myrun.feature.home.ui.HomeScreenDimensions.FabSize
import akio.apps.myrun.feature.usertimeline.impl.UserTimelineViewModel
import akio.apps.myrun.feature.usertimeline.model.Activity
import akio.apps.myrun.feature.usertimeline.ui.UserTimeline
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.DropdownMenu
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.sharp.CheckCircleOutline
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.google.accompanist.insets.LocalWindowInsets
import kotlin.math.roundToInt
import kotlinx.coroutines.launch

object HomeScreenDimensions {
    val AppBarHeight = 56.dp
    val FabSize = 56.dp
}

object HomeScreenColors {
    val uploadingBadgeContentColor = Color(0xffffffff)
}

@Composable
fun HomeScreen(
    onClickFloatingActionButton: () -> Unit,
    onClickActivityItemAction: (Activity) -> Unit,
    onClickExportActivityFile: (Activity) -> Unit,
    navController: NavController,
    userTimelineViewModel: UserTimelineViewModel,
) = AppTheme {
    val insets = LocalWindowInsets.current
    val topBarHeightDp = AppBarHeight + insets.systemBars.top.px2dp.dp
    val topBarHeightPx = with(LocalDensity.current) { topBarHeightDp.roundToPx().toFloat() }
    val fabBoxHeightDp = FabSize * 4 / 3 + insets.systemBars.bottom.px2dp.dp
    val fabBoxSizePx = with(LocalDensity.current) { fabBoxHeightDp.roundToPx().toFloat() }
    var topBarOffsetY by remember { mutableStateOf(0f) }
    var fabOffsetY by remember { mutableStateOf(0f) }

    // insets may be updated
    val nestedScrollConnection = remember(topBarHeightPx, fabBoxSizePx) {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                val delta = available.y
                var newOffset = topBarOffsetY + delta
                topBarOffsetY = newOffset.coerceIn(-topBarHeightPx, 0f)
                newOffset = fabOffsetY - delta
                fabOffsetY = newOffset.coerceIn(0f, fabBoxSizePx)
                return Offset.Zero
            }
        }
    }
    val activityStorageCount by userTimelineViewModel.activityUploadBadge
        .collectAsState(initial = null)
    val feedListState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    Box(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(nestedScrollConnection)
    ) {
        UserTimeline(
            userTimelineViewModel,
            PaddingValues(
                top = topBarHeightDp,
                bottom = fabBoxHeightDp // avoid the bottom bar
            ),
            feedListState,
            onClickActivityItemAction,
            onClickExportActivityFile,
            navController
        )
        HomeTopBar(
            activityStorageCount,
            navController,
            { coroutineScope.launch { feedListState.animateScrollToItem(0) } },
            Modifier
                .height(topBarHeightDp)
                .align(Alignment.TopCenter)
                .offset { IntOffset(x = 0, y = topBarOffsetY.roundToInt()) }
                .background(AppColors.primary)
        )
        Box(
            modifier = Modifier
                .height(fabBoxHeightDp)
                .align(Alignment.BottomCenter)
                .offset { IntOffset(x = 0, y = fabOffsetY.roundToInt()) }
        ) {
            HomeFloatingActionButton(onClickFloatingActionButton)
        }
    }
}

@Composable
private fun UploadInProgressBadge(count: Int) {
    var isUploadInfoPopupShowing by remember { mutableStateOf(false) }
    Box(
        modifier = Modifier
            .size(AppBarHeight)
            .clickable { isUploadInfoPopupShowing = true }
            .padding(AppDimensions.iconButtonPadding),
        contentAlignment = Alignment.Center
    ) {
        LinearProgressIndicator(
            modifier = Modifier
                .fillMaxWidth()
                .height(2.dp)
                .align(Alignment.BottomCenter),
            color = uploadingBadgeContentColor
        )
        Text(
            text = "$count",
            fontWeight = FontWeight.Black,
            fontSize = 11.sp,
            color = uploadingBadgeContentColor
        )
        DropdownMenu(
            modifier = Modifier
                .background(Color.Black)
                .padding(horizontal = 8.dp),
            expanded = isUploadInfoPopupShowing,
            onDismissRequest = { isUploadInfoPopupShowing = false },
        ) {
            val popupMessage = stringResource(R.string.home_upload_progress, count)
            Text(
                text = popupMessage,
                color = Color.White,
                fontSize = 12.sp
            )
        }
    }
}

@Preview
@Composable
private fun PreviewUploadingBadge() = UploadInProgressBadge(count = 3)

@Composable
private fun HomeTopBar(
    activityUploadBadge: UserTimelineViewModel.ActivityUploadBadgeStatus?,
    navController: NavController,
    onClickUploadCompleteBadge: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier, contentAlignment = Alignment.BottomCenter) {
        TopAppBar(
            title = {
                Text(
                    stringResource(id = R.string.user_feed_title),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = Icons.Rounded.Settings.defaultWidth)
                )
            },
            actions = {
                ActivityUploadNotifierBadge(activityUploadBadge, onClickUploadCompleteBadge)
                AppBarIconButton(Icons.Rounded.Settings) {
                    navController.navigate(
                        MainNavigationDestination.Profile.routeWithUserId(null)
                    )
                }
            }
        )
    }
}

@Composable
private fun ActivityUploadNotifierBadge(
    activityUploadBadge: UserTimelineViewModel.ActivityUploadBadgeStatus?,
    onClickUploadCompleteBadge: () -> Unit,
) {
    Crossfade(targetState = activityUploadBadge) {
        when (it) {
            is UserTimelineViewModel.ActivityUploadBadgeStatus.InProgress -> {
                UploadInProgressBadge(count = it.activityCount)
            }
            UserTimelineViewModel.ActivityUploadBadgeStatus.Complete -> {
                UploadCompleteBadge(onClickUploadCompleteBadge)
            }
            else -> {
                // render void
            }
        }
    }
}

@Composable
private fun UploadCompleteBadge(onClickUploadCompleteBadge: () -> Unit) {
    var isDismissed by remember { mutableStateOf(false) }
    if (isDismissed) {
        return
    }
    AppBarIconButton(
        iconImageVector = Icons.Sharp.CheckCircleOutline
    ) {
        onClickUploadCompleteBadge()
        isDismissed = true
    }
}

@Composable
private fun HomeFloatingActionButton(onClick: () -> Unit, modifier: Modifier = Modifier) =
    FloatingActionButton(onClick = onClick, modifier = modifier) {
        Icon(
            imageVector = Icons.Rounded.Add,
            contentDescription = "Floating action button on bottom bar"
        )
    }

@Preview
@Composable
private fun PreviewTopBar() = HomeTopBar(
    activityUploadBadge = UserTimelineViewModel.ActivityUploadBadgeStatus.Complete,
    rememberNavController(),
    {}
)
