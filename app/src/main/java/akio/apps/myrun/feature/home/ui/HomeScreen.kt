package akio.apps.myrun.feature.home.ui

import akio.apps.myrun.R
import akio.apps.myrun.feature.usertimeline.UserTimelineViewModel
import akio.apps.myrun.feature.usertimeline.model.Activity
import akio.apps.myrun.feature.usertimeline.ui.UserTimeline
import akio.apps.myrun.ui.theme.AppColors
import akio.apps.myrun.ui.theme.AppTheme
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material.BottomAppBar
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

private val AppBarHeight = 56.dp
private val FabSize = 56.dp

@Composable
fun HomeScreen(
    userTimelineViewModel: UserTimelineViewModel,
    contentPaddings: PaddingValues,
    onClickUserProfileButton: () -> Unit,
    onClickFloatingActionButton: () -> Unit,
    onClickActivityItemAction: (Activity) -> Unit,
    onClickActivityFileAction: (Activity) -> Unit,
    onClickUserAvatar: (String) -> Unit
) {
    AppTheme {
        val topBarHeightDp = AppBarHeight + contentPaddings.calculateTopPadding()
        val topBarHeightPx = with(LocalDensity.current) { topBarHeightDp.roundToPx().toFloat() }
        val fabBoxHeightDp = FabSize * 4 / 3 + contentPaddings.calculateBottomPadding()
        val fabBoxSizePx = with(LocalDensity.current) { fabBoxHeightDp.roundToPx().toFloat() }
        var topBarOffsetY by remember { mutableStateOf(0f) }
        var fabOffsetY by remember { mutableStateOf(0f) }
        val nestedScrollConnection = remember {
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
                onClickActivityItemAction,
                onClickActivityFileAction,
                onClickUserAvatar
            )
            HomeTopBar(
                onClickUserProfileButton,
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
}

@Composable
private fun HomeTopBar(onClickUserProfileButton: () -> Unit, modifier: Modifier = Modifier) =
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
                AppBarIconButton(onClickUserProfileButton, Icons.Rounded.Settings)
            }
        )
    }

@Composable
private fun HomeFloatingActionButton(onClick: () -> Unit, modifier: Modifier = Modifier) =
    FloatingActionButton(onClick = onClick, modifier = modifier) {
        Icon(
            imageVector = Icons.Rounded.Add,
            contentDescription = "Floating action button on bottom bar"
        )
    }

@Composable
private fun HomeBottomBar(
    onClickUserProfileButton: () -> Unit
) = BottomAppBar(elevation = 4.dp) {
    AppBarIconButton(onClickUserProfileButton, Icons.Rounded.Settings)
    AppBarIconButton(onClickUserProfileButton, Icons.Rounded.Settings)
    AppBarIconButton(onClickUserProfileButton, Icons.Rounded.Settings)
}

@Composable
private fun AppBarIconButton(onClick: () -> Unit, iconImageVector: ImageVector) =
    IconButton(onClick = onClick) {
        Icon(
            imageVector = iconImageVector,
            contentDescription = "User profile icon button on bottom bar"
        )
    }

@Preview
@Composable
private fun PreviewBottomBar() = HomeBottomBar { }
