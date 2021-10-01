package akio.apps.myrun.feature.home.ui

import akio.apps.myrun.R
import akio.apps.myrun.data.activity.api.model.ActivityModel
import akio.apps.myrun.feature.base.ui.AppDimensions.AppBarHeight
import akio.apps.myrun.feature.base.ui.AppDimensions.FabSize
import akio.apps.myrun.feature.base.ui.AppTheme
import akio.apps.myrun.feature.base.ui.NavigationBarSpacer
import akio.apps.myrun.feature.base.ui.px2dp
import akio.apps.myrun.feature.base.viewmodel.savedStateViewModelProvider
import akio.apps.myrun.feature.base.viewmodel.viewModelProvider
import akio.apps.myrun.feature.home._di.DaggerHomeFeatureComponent
import akio.apps.myrun.feature.home.feed.ui.ActivityFeed
import akio.apps.myrun.feature.home.userhome._di.DaggerUserHomeFeatureComponent
import akio.apps.myrun.feature.home.userhome.ui.UserHome
import androidx.annotation.StringRes
import androidx.compose.animation.core.Animatable
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
import androidx.compose.material.icons.rounded.Home
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
    UserHome(
        label = R.string.home_nav_user_home_tab_label,
        icon = Icons.Rounded.Home,
        route = "userHome"
    )
}

private const val REVEAL_ANIM_THRESHOLD = 10

@Composable
fun HomeScreen(
    onClickFloatingActionButton: () -> Unit,
    onClickExportActivityFile: (ActivityModel) -> Unit,
    appNavController: NavController,
) = AppTheme {
    // FAB is inactive when user selects a tab other than Feed
    var isFabActive by remember { mutableStateOf(true) }
    val insets = LocalWindowInsets.current
    val fabBoxHeightDp = FabSize * 4 / 3 + AppBarHeight + insets.systemBars.bottom.px2dp.dp
    val fabBoxSizePx = with(LocalDensity.current) { fabBoxHeightDp.roundToPx().toFloat() }
    val fabOffsetY = remember { Animatable(0f) }
    val coroutineScope = rememberCoroutineScope()
    // insets may be updated, so remember scroll connection with keys
    val nestedScrollConnection = remember(fabBoxSizePx) {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                val delta = available.y
                if (!isFabActive) {
                    return Offset.Zero
                }

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
    }

    val homeNavController = rememberNavController()
    val currentBackStackEntry by homeNavController.currentBackStackEntryAsState()
    isFabActive = currentBackStackEntry?.destination?.route == HomeNavItemInfo.ActivityFeed.route
    // toggle FAB when switching between tabs
    LaunchedEffect(isFabActive) {
        when {
            isFabActive && fabOffsetY.value != 0f -> {
                fabOffsetY.animateTo(0f)
            }
            !isFabActive && fabOffsetY.value != fabBoxSizePx -> {
                fabOffsetY.animateTo(fabBoxSizePx)
            }
        }
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(nestedScrollConnection)
    ) {
        HomeNavHost(
            homeNavController,
            onClickExportActivityFile,
            PaddingValues(bottom = fabBoxHeightDp),
            appNavController
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

@Composable
private fun HomeNavHost(
    homeNavController: NavHostController,
    onClickExportActivityFile: (ActivityModel) -> Unit,
    contentPaddings: PaddingValues,
    appNavController: NavController,
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

        addUserHomeDestination(contentPaddings, appNavController)
    }
}

private fun NavGraphBuilder.addUserHomeDestination(
    contentPaddings: PaddingValues,
    appNavController: NavController,
) {
    composable(route = HomeNavItemInfo.UserHome.route) { backstackEntry ->
        val userHomeViewModel = backstackEntry.savedStateViewModelProvider(backstackEntry) {
            DaggerUserHomeFeatureComponent.factory().create(it).userHomeViewModel()
        }
        UserHome(userHomeViewModel, contentPaddings, appNavController)
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
    onClickExportActivityFile: (ActivityModel) -> Unit,
    appNavController: NavController,
) {
    composable(route = HomeNavItemInfo.ActivityFeed.route) { backstackEntry ->
        val diComponent = remember { DaggerHomeFeatureComponent.factory().create() }
        val userTimelineViewModel = backstackEntry.viewModelProvider {
            diComponent.activityFeedViewModel()
        }
        ActivityFeed(
            activityFeedViewModel = userTimelineViewModel,
            contentPadding = contentPadding,
            onClickExportActivityFile = onClickExportActivityFile,
            navController = appNavController
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
