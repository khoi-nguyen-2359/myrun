package akio.apps.myrun.feature.main.ui

import akio.apps.myrun.R
import akio.apps.myrun.data.activity.api.model.BaseActivityModel
import akio.apps.myrun.feature.core.ktx.px2dp
import akio.apps.myrun.feature.core.navigation.HomeTabNavDestination
import akio.apps.myrun.feature.core.ui.AppDimensions.AppBarHeight
import akio.apps.myrun.feature.core.ui.AppDimensions.FabSize
import akio.apps.myrun.feature.core.ui.AppTheme
import akio.apps.myrun.feature.core.ui.NavigationBarSpacer
import akio.apps.myrun.feature.feed.ui.ActivityFeedComposable
import akio.apps.myrun.feature.main.HomeTabViewModel
import akio.apps.myrun.feature.main.di.DaggerHomeTabFeatureComponent
import akio.apps.myrun.feature.userstats.ui.CurrentUserStatsComposable
import android.app.Application
import androidx.annotation.StringRes
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.systemBars
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.material.icons.rounded.DirectionsRun
import androidx.compose.material.icons.rounded.Timeline
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.google.accompanist.navigation.animation.composable
import com.google.accompanist.navigation.animation.rememberAnimatedNavController
import kotlin.math.roundToInt

private object HomeTabNavTransitionDefaults {
    private val fadeInImmediately = fadeIn(
        initialAlpha = 1f,
        animationSpec = tween(durationMillis = 0)
    )
    private val fadeOutImmediately = fadeOut(
        targetAlpha = 0f,
        animationSpec = tween(durationMillis = 0)
    )

    val enterTransition: EnterTransition = fadeInImmediately
    val exitTransition: ExitTransition = fadeOutImmediately
    val popEnterTransition: EnterTransition = fadeInImmediately
    val popExitTransition: ExitTransition = fadeOutImmediately
}

internal enum class HomeNavItemInfo(
    @StringRes
    val label: Int,
    val icon: ImageVector,
    val route: String,
) {
    ActivityFeed(
        label = R.string.home_nav_activity_feed_tab_label,
        icon = Icons.Rounded.Timeline,
        route = HomeTabNavDestination.Feed.route
    ),
    UserStats(
        label = R.string.home_nav_user_stats_tab_label,
        icon = Icons.Rounded.BarChart,
        route = HomeTabNavDestination.Stats.route
    ),
}

@Composable
fun HomeTabComposable(
    appNavController: NavController,
    onClickFloatingActionButton: () -> Unit,
    onClickExportActivityFile: (BaseActivityModel) -> Unit,
    openRoutePlanningAction: () -> Unit,
    viewModel: HomeTabViewModel = rememberViewModel(),
) = AppTheme {
    val navigator = rememberNavigator()
    val fabState = rememberFabState(navigator.currentTabNavEntry)
    // toggle FAB when switching between tabs
    LaunchedEffect(fabState.isFabActive) {
        fabState.toggleFabAnimation()
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(fabState.fabAnimationScrollConnection)
    ) {
        HomeTabNavHost(
            navigator.homeNavHostController,
            onClickExportActivityFile,
            fabState.fabBoxHeightDp,
            appNavController,
            openRoutePlanningAction
        )

        val isTrackingStarted by viewModel.isTrackingStartedFlow.collectAsState(initial = false)
        HomeFabBox(
            isTrackingStarted,
            fabState.fabBoxHeightDp,
            fabState.fabOffsetYAnimatable,
            onClickFloatingActionButton
        )

        Column(modifier = Modifier.align(Alignment.BottomCenter)) {
            HomeBottomNavBar(navigator)
            NavigationBarSpacer()
        }
    }
}

@Composable
private fun rememberViewModel(): HomeTabViewModel {
    val application = LocalContext.current.applicationContext as Application
    return remember {
        DaggerHomeTabFeatureComponent.factory().create(application).homeTabViewModel()
    }
}

@Composable
private fun rememberFabState(currentTabEntry: NavBackStackEntry?): HomeTabFabState {
    val systemBarBottomDp = WindowInsets.systemBars.getBottom(LocalDensity.current).px2dp.dp
    val fabBoxHeightDp = FabSize * 4f / 3 + AppBarHeight + systemBarBottomDp
    val fabBoxHeightPx = with(LocalDensity.current) { fabBoxHeightDp.roundToPx().toFloat() }
    val isFabActive by derivedStateOf {
        currentTabEntry?.destination?.route == HomeNavItemInfo.ActivityFeed.route
    }
    val coroutineScope = rememberCoroutineScope()
    val fabOffsetYAnimatable = remember { Animatable(0f) }
    return remember(fabBoxHeightDp, isFabActive) {
        HomeTabFabState(
            coroutineScope,
            fabOffsetYAnimatable,
            fabBoxHeightDp,
            fabBoxHeightPx,
            isFabActive
        )
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
private fun rememberNavigator(
    homeNavHostController: NavHostController = rememberAnimatedNavController(),
): HomeTabNavigator {
    val currentTabNavEntry: NavBackStackEntry?
        by homeNavHostController.currentBackStackEntryAsState()
    return remember(homeNavHostController, currentTabNavEntry) {
        HomeTabNavigator(homeNavHostController, currentTabNavEntry)
    }
}

@Composable
private fun BoxScope.HomeFabBox(
    isTrackingStarted: Boolean,
    fabBoxHeightDp: Dp,
    fabOffsetYAnimatable: Animatable<Float, AnimationVector1D>,
    onClickFloatingActionButton: () -> Unit,
) {
    Box(
        modifier = Modifier
            .height(fabBoxHeightDp)
            .align(Alignment.BottomCenter)
            .offset { IntOffset(x = 0, y = fabOffsetYAnimatable.value.roundToInt()) }
    ) {
        HomeFloatingActionButton(onClickFloatingActionButton, isTrackingStarted)
    }
}

@Composable
private fun HomeBottomNavBar(navigator: HomeTabNavigator) {
    BottomNavigation {
        HomeNavItemInfo.values().forEach { itemInfo ->
            val isSelected = navigator.isSelectedDestination(itemInfo)
            BottomNavigationItem(
                selected = isSelected,
                onClick = { navigator.navigateHomeDestination(itemInfo) },
                icon = { Icon(itemInfo.icon, "Home tab icon") },
                label = { Text(text = stringResource(id = itemInfo.label)) }
            )
        }
    }
}

@Composable
private fun HomeFloatingActionButton(
    onClick: () -> Unit,
    isTrackingStarted: Boolean,
    modifier: Modifier = Modifier,
) {
    val fabIcon = if (isTrackingStarted) {
        Icons.Rounded.DirectionsRun
    } else {
        Icons.Rounded.Add
    }
    FloatingActionButton(onClick = onClick, modifier = modifier) {
        Icon(
            imageVector = fabIcon,
            contentDescription = "Floating action button on bottom bar"
        )
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
private fun HomeTabNavHost(
    homeNavController: NavHostController,
    onClickExportActivityFile: (BaseActivityModel) -> Unit,
    contentPaddingBottom: Dp,
    appNavController: NavController,
    openRoutePlanningAction: () -> Unit,
) {
    AnimatedNavHost(
        modifier = Modifier.fillMaxSize(),
        navController = homeNavController,
        startDestination = HomeNavItemInfo.ActivityFeed.route,
        enterTransition = { HomeTabNavTransitionDefaults.enterTransition },
        exitTransition = { HomeTabNavTransitionDefaults.exitTransition },
        popEnterTransition = { HomeTabNavTransitionDefaults.popEnterTransition },
        popExitTransition = { HomeTabNavTransitionDefaults.popExitTransition }
    ) {
        composable(HomeNavItemInfo.ActivityFeed.route) {
            ActivityFeedComposable(
                appNavController,
                homeNavController,
                contentPaddingBottom,
                onClickExportActivityFile
            )
        }

        composable(HomeNavItemInfo.UserStats.route) {
            CurrentUserStatsComposable(
                appNavController,
                contentPaddingBottom,
                openRoutePlanningAction
            )
        }
    }
}
