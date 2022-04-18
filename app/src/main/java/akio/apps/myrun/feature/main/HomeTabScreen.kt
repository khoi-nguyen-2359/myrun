package akio.apps.myrun.feature.main

import akio.apps.myrun.R
import akio.apps.myrun.data.activity.api.model.BaseActivityModel
import akio.apps.myrun.feature.core.ktx.px2dp
import akio.apps.myrun.feature.core.ui.AppDimensions.AppBarHeight
import akio.apps.myrun.feature.core.ui.AppDimensions.FabSize
import akio.apps.myrun.feature.core.ui.AppTheme
import akio.apps.myrun.feature.core.ui.NavigationBarSpacer
import akio.apps.myrun.feature.feed.ui.ActivityFeedScreen
import akio.apps.myrun.feature.userstats.ui.UserStatsScreen
import androidx.annotation.StringRes
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
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
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.material.icons.rounded.Timeline
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.google.accompanist.insets.LocalWindowInsets
import kotlin.math.roundToInt
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

private enum class HomeNavItemInfo(
    @StringRes
    val label: Int,
    val icon: ImageVector,
    val route: String,
) {
    ActivityFeed(
        label = R.string.home_nav_activity_feed_tab_label,
        icon = Icons.Rounded.Timeline,
        route = "activityFeed"
    ),
    UserStats(
        label = R.string.home_nav_user_stats_tab_label,
        icon = Icons.Rounded.BarChart,
        route = "userStats"
    )
}

private const val REVEAL_ANIM_THRESHOLD = 10

@Composable
fun HomeTabScreen(
    appNavController: NavController,
    onClickFloatingActionButton: () -> Unit,
    onClickExportActivityFile: (BaseActivityModel) -> Unit,
    openRoutePlanningAction: () -> Unit,
) = AppTheme {
    // FAB is inactive when user selects a tab other than Feed
    var isFabActive by remember { mutableStateOf(true) }
    val insets = LocalWindowInsets.current
    val fabBoxHeightDp = FabSize * 4 / 3 + AppBarHeight + insets.systemBars.bottom.px2dp.dp
    val fabBoxSizePx = with(LocalDensity.current) { fabBoxHeightDp.roundToPx().toFloat() }
    val fabOffsetY = remember { Animatable(0f) }
    val coroutineScope = rememberCoroutineScope()
    // insets may be updated, so remember scroll connection with keys
    val fabAnimationScrollConnection = remember(fabBoxSizePx) {
        createScrollConnectionForFabAnimation(isFabActive, fabBoxSizePx, coroutineScope, fabOffsetY)
    }

    val homeNavController = rememberNavController()
    val currentBackStackEntry by homeNavController.currentBackStackEntryAsState()
    isFabActive = currentBackStackEntry?.destination?.route == HomeNavItemInfo.ActivityFeed.route
    // toggle FAB when switching between tabs
    LaunchedEffect(isFabActive) {
        animateFabOffsetY(isFabActive, fabOffsetY, fabBoxSizePx)
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(fabAnimationScrollConnection)
    ) {
        HomeNavHost(
            homeNavController,
            onClickExportActivityFile,
            PaddingValues(bottom = fabBoxHeightDp),
            appNavController,
            openRoutePlanningAction
        )
        Box(
            modifier = Modifier
                .height(fabBoxHeightDp)
                .align(Alignment.BottomCenter)
                .offset { IntOffset(x = 0, y = fabOffsetY.value.roundToInt()) }
        ) {
            HomeFloatingActionButton(onClickFloatingActionButton)
        }

        Column(modifier = Modifier.align(Alignment.BottomCenter)) {
            HomeBottomNavBar(homeNavController)
            NavigationBarSpacer()
        }
    }
}

private fun createScrollConnectionForFabAnimation(
    isFabActive: Boolean,
    fabBoxSizePx: Float,
    coroutineScope: CoroutineScope,
    fabOffsetY: Animatable<Float, AnimationVector1D>,
) = object : NestedScrollConnection {
    override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
        if (!isFabActive) {
            return Offset.Zero
        }
        val delta = available.y
        val targetFabOffsetY = when {
            delta >= REVEAL_ANIM_THRESHOLD -> 0f // reveal (move up)
            delta <= -REVEAL_ANIM_THRESHOLD -> fabBoxSizePx // go away (move down)
            else -> return Offset.Zero
        }
        coroutineScope.launch {
            fabOffsetY.animateTo(targetFabOffsetY)
        }
        return Offset.Zero
    }
}

private suspend fun animateFabOffsetY(
    isFabActive: Boolean,
    fabOffsetY: Animatable<Float, AnimationVector1D>,
    fabBoxSizePx: Float,
) {
    when {
        isFabActive && fabOffsetY.value != 0f -> {
            fabOffsetY.animateTo(0f)
        }
        !isFabActive && fabOffsetY.value != fabBoxSizePx -> {
            fabOffsetY.animateTo(fabBoxSizePx)
        }
    }
}

@Composable
private fun HomeNavHost(
    homeNavController: NavHostController,
    onClickExportActivityFile: (BaseActivityModel) -> Unit,
    contentPaddings: PaddingValues,
    appNavController: NavController,
    openRoutePlanningAction: () -> Unit,
) {
    NavHost(
        modifier = Modifier.fillMaxSize(),
        navController = homeNavController,
        startDestination = HomeNavItemInfo.ActivityFeed.route
    ) {
        addActivityFeedDestination(
            contentPadding = contentPaddings,
            onClickExportActivityFile = onClickExportActivityFile,
            appNavController = appNavController
        )

        addUserStatsDestination(contentPaddings, appNavController, openRoutePlanningAction)
    }
}

private fun NavGraphBuilder.addUserStatsDestination(
    contentPaddings: PaddingValues,
    appNavController: NavController,
    openRoutePlanningAction: () -> Unit,
) {
    composable(route = HomeNavItemInfo.UserStats.route) { backstackEntry ->
        UserStatsScreen(appNavController, backstackEntry, contentPaddings, openRoutePlanningAction)
    }
}

@Composable
private fun HomeBottomNavBar(homeNavController: NavHostController) {
    val currentBackstackEntry by homeNavController.currentBackStackEntryAsState()
    BottomNavigation {
        HomeNavItemInfo.values().forEach { itemInfo ->
            val isSelected = currentBackstackEntry?.destination
                ?.hierarchy
                ?.any { it.route == itemInfo.route } == true
            BottomNavigationItem(
                selected = isSelected,
                onClick = { navigateHomeDestination(homeNavController, itemInfo) },
                icon = { Icon(itemInfo.icon, "Home tab icon") },
                label = { Text(text = stringResource(id = itemInfo.label)) }
            )
        }
    }
}

private fun navigateHomeDestination(
    homeNavController: NavHostController,
    itemInfo: HomeNavItemInfo,
) {
    homeNavController.navigate(itemInfo.route) {
        popUpTo(homeNavController.graph.findStartDestination().id) {
            saveState = true
        }
        launchSingleTop = true
        restoreState = true
    }
}

private fun NavGraphBuilder.addActivityFeedDestination(
    contentPadding: PaddingValues,
    onClickExportActivityFile: (BaseActivityModel) -> Unit,
    appNavController: NavController,
) {
    composable(route = HomeNavItemInfo.ActivityFeed.route) { backstackEntry ->
        ActivityFeedScreen(
            appNavController,
            backstackEntry,
            contentPadding = contentPadding,
            onClickExportActivityFile = onClickExportActivityFile
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
