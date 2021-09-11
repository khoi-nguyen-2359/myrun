package akio.apps.myrun.feature.home.ui

import akio.apps.common.feature.ui.px2dp
import akio.apps.common.feature.viewmodel.viewModelProvider
import akio.apps.myrun.R
import akio.apps.myrun.data.activity.api.model.ActivityModel
import akio.apps.myrun.feature.base.ui.AppTheme
import akio.apps.myrun.feature.base.ui.NavigationBarSpacer
import akio.apps.myrun.feature.home._di.DaggerHomeFeatureComponent
import akio.apps.myrun.feature.home.navigation.HomeTabNavDestinationInfo
import akio.apps.myrun.feature.home.ui.HomeScreenDimensions.AppBarHeight
import akio.apps.myrun.feature.home.ui.HomeScreenDimensions.FabSize
import akio.apps.myrun.feature.userhome.ui.UserHome
import akio.apps.myrun.feature.usertimeline.ui.ActivityFeed
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.sharp.Home
import androidx.compose.material.icons.sharp.Timeline
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.accompanist.insets.LocalWindowInsets
import kotlin.math.roundToInt

object HomeScreenDimensions {
    val AppBarHeight = 56.dp
    val FabSize = 56.dp
}

object HomeScreenColors {
    val uploadingBadgeContentColor = Color(0xffffffff)
}

private enum class HomeNavItemInfo(
    @StringRes
    val label: Int,
    val icon: ImageVector,
    val navInfo: HomeTabNavDestinationInfo,
) {
    ActivityFeed(
        label = R.string.home_nav_activity_feed_tab_label,
        icon = Icons.Sharp.Timeline,
        navInfo = HomeTabNavDestinationInfo.ActivityFeedTab
    ),
    UserHome(
        label = R.string.home_nav_user_home_tab_label,
        icon = Icons.Sharp.Home,
        navInfo = HomeTabNavDestinationInfo.UserHomeTab
    )
}

@Suppress("UNUSED_PARAMETER")
@Composable
fun HomeScreen(
    onClickFloatingActionButton: () -> Unit,
    onClickExportActivityFile: (ActivityModel) -> Unit,
    appNavController: NavController,
) = AppTheme {
    val insets = LocalWindowInsets.current
    val fabBoxHeightDp = FabSize * 4 / 3 + AppBarHeight + insets.systemBars.bottom.px2dp.dp
    val fabBoxSizePx = with(LocalDensity.current) { fabBoxHeightDp.roundToPx().toFloat() }
    var fabOffsetY by remember { mutableStateOf(0f) }

    // insets may be updated, so remember scroll connection with keys
    val nestedScrollConnection = remember(fabBoxSizePx) {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                val delta = available.y
                val newOffset = fabOffsetY - delta
                fabOffsetY = newOffset.coerceIn(0f, fabBoxSizePx)
                return Offset.Zero
            }
        }
    }
    val homeNavController = rememberNavController()
    Box(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(nestedScrollConnection)
    ) {
        NavHost(
            modifier = Modifier.fillMaxSize(),
            navController = homeNavController,
            startDestination = HomeTabNavDestinationInfo.ActivityFeedTab.route
        ) {
            addActivityFeedDestination(
                contentPadding = PaddingValues(bottom = fabBoxHeightDp),
                onClickExportActivityFile = onClickExportActivityFile,
                navController = appNavController
            )

            addUserHomeDestination()
        }
        Box(
            modifier = Modifier
                .height(fabBoxHeightDp)
                .align(Alignment.BottomCenter)
                .offset { IntOffset(x = 0, y = fabOffsetY.roundToInt()) }
        ) {
            HomeFloatingActionButton(onClickFloatingActionButton)
        }

        HomeBottomNavBar(
            modifier = Modifier.align(Alignment.BottomCenter),
            homeNavController
        )
    }
}

private fun NavGraphBuilder.addUserHomeDestination() {
    composable(
        route = HomeTabNavDestinationInfo.UserHomeTab.route,
        arguments = HomeTabNavDestinationInfo.UserHomeTab.arguments
    ) {
        UserHome()
    }
}

@Composable
private fun HomeBottomNavBar(
    modifier: Modifier = Modifier,
    homeNavController: NavHostController,
) {
    Column(modifier = modifier) {
        BottomNavigation {
            HomeNavItemInfo.values().forEach { itemInfo ->
                BottomNavigationItem(
                    selected = true,
                    onClick = {
                        homeNavController.navigate(itemInfo.navInfo.route) {
                            popUpTo(itemInfo.navInfo.route)
                            launchSingleTop = true
                        }
                    },
                    icon = { Icon(itemInfo.icon, "") },
                    label = { Text(text = stringResource(id = itemInfo.label)) }
                )
            }
        }
        NavigationBarSpacer()
    }
}

private fun NavGraphBuilder.addActivityFeedDestination(
    contentPadding: PaddingValues,
    onClickExportActivityFile: (ActivityModel) -> Unit,
    navController: NavController,
) {
    composable(
        route = HomeTabNavDestinationInfo.ActivityFeedTab.route,
        arguments = HomeTabNavDestinationInfo.ActivityFeedTab.arguments
    ) { backstackEntry ->
        val diComponent = remember { DaggerHomeFeatureComponent.factory().create() }
        val userTimelineViewModel = backstackEntry.viewModelProvider {
            diComponent.activityFeedViewModel()
        }
        ActivityFeed(
            userTimelineViewModel = userTimelineViewModel,
            contentPadding = contentPadding,
            onClickExportActivityFile = onClickExportActivityFile,
            navController = navController
        )
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
