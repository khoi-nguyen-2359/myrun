package akio.apps.myrun.feature.main.ui

import akio.apps.myrun.R
import akio.apps.myrun.data.activity.api.model.BaseActivityModel
import akio.apps.myrun.feature.core.ktx.px2dp
import akio.apps.myrun.feature.core.ktx.rememberViewModelProvider
import akio.apps.myrun.feature.core.navigation.HomeTabNavDestination
import akio.apps.myrun.feature.core.ui.AppDimensions.AppBarHeight
import akio.apps.myrun.feature.core.ui.AppDimensions.FabSize
import akio.apps.myrun.feature.core.ui.AppTheme
import akio.apps.myrun.feature.core.ui.NavigationBarSpacer
import akio.apps.myrun.feature.feed.ui.ActivityFeedScreen
import akio.apps.myrun.feature.main.di.DaggerHomeTabFeatureComponent
import akio.apps.myrun.feature.userstats.ui.CurrentUserStatsScreen
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
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.google.accompanist.navigation.animation.composable
import com.google.accompanist.navigation.animation.rememberAnimatedNavController
import kotlin.math.roundToInt
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

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

private enum class HomeNavItemInfo(
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
    )
}

private const val REVEAL_ANIM_THRESHOLD = 10

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun HomeTabScreen(
    appNavController: NavController,
    backStackEntry: NavBackStackEntry,
    onClickFloatingActionButton: () -> Unit,
    onClickExportActivityFile: (BaseActivityModel) -> Unit,
    openRoutePlanningAction: () -> Unit,
) = AppTheme {
    // FAB is inactive when user selects a tab other than Feed
    var isFabActive by remember { mutableStateOf(true) }
    val systemBarTopDp = WindowInsets.systemBars.getBottom(LocalDensity.current).px2dp.dp
    val fabBoxHeightDp = FabSize * 4 / 3 + AppBarHeight + systemBarTopDp
    val fabBoxSizePx = with(LocalDensity.current) { fabBoxHeightDp.roundToPx().toFloat() }
    val fabOffsetY = remember { Animatable(0f) }
    val coroutineScope = rememberCoroutineScope()
    // insets may be updated, so remember scroll connection with keys
    val fabAnimationScrollConnection = remember(fabBoxSizePx) {
        createScrollConnectionForFabAnimation(isFabActive, fabBoxSizePx, coroutineScope, fabOffsetY)
    }

    val homeNavController = rememberAnimatedNavController()
    val homeBackStackEntry by homeNavController.currentBackStackEntryAsState()
    isFabActive = homeBackStackEntry?.destination?.route == HomeNavItemInfo.ActivityFeed.route
    // toggle FAB when switching between tabs
    LaunchedEffect(isFabActive) {
        animateFabOffsetY(isFabActive, fabOffsetY, fabBoxSizePx)
    }
    val application = LocalContext.current.applicationContext as Application
    val homeTabViewModel = backStackEntry.rememberViewModelProvider {
        DaggerHomeTabFeatureComponent.factory().create(application).homeTabViewModel()
    }
    val isTrackingStarted by homeTabViewModel.isTrackingStarted.collectAsState(false)
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
            HomeFloatingActionButton(onClickFloatingActionButton, isTrackingStarted)
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

@OptIn(ExperimentalAnimationApi::class)
@Composable
private fun HomeNavHost(
    homeNavController: NavHostController,
    onClickExportActivityFile: (BaseActivityModel) -> Unit,
    contentPaddings: PaddingValues,
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
        composable(HomeNavItemInfo.ActivityFeed.route) { navEntry ->
            ActivityFeedScreen(
                appNavController,
                homeNavController,
                navEntry,
                contentPaddings,
                onClickExportActivityFile
            )
        }

        composable(HomeNavItemInfo.UserStats.route) { navEntry ->
            CurrentUserStatsScreen(
                appNavController,
                navEntry,
                contentPaddings,
                openRoutePlanningAction
            )
        }
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
    val currentEntryDesId = homeNavController.currentBackStackEntry?.destination?.id
        ?: homeNavController.graph.findStartDestination().id
    homeNavController.navigate(itemInfo.route) {
        popUpTo(currentEntryDesId) {
            inclusive = true
            saveState = true
        }
        launchSingleTop = true
        restoreState = true
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
