package akio.apps.myrun.feature.home.ui

import akio.apps.myrun.data.activity.api.model.ActivityModel
import akio.apps.myrun.feature.activitydetail.ui.ActivityDetailScreen
import akio.apps.myrun.feature.base.navigation.HomeNavDestination
import akio.apps.myrun.feature.profile.ui.UserProfileScreen
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import com.google.accompanist.insets.ProvideWindowInsets
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.google.accompanist.navigation.animation.composable
import com.google.accompanist.navigation.animation.rememberAnimatedNavController

@OptIn(ExperimentalAnimationApi::class)
private object AppNavTransitionDefaults {
    val enterTransition: EnterTransition = slideInHorizontally(
        initialOffsetX = { fullWidth -> fullWidth / 2 },
        animationSpec = tween(200, easing = LinearEasing)
    ) + fadeIn(initialAlpha = 0f, animationSpec = tween(200, easing = LinearEasing))

    val popEnterTransition: EnterTransition = slideInHorizontally(
        initialOffsetX = { 0 },
        animationSpec = tween(200, easing = LinearEasing)
    ) + fadeIn(initialAlpha = 1f, animationSpec = tween(200, easing = LinearEasing))

    val popExitTransition: ExitTransition = slideOutHorizontally(
        targetOffsetX = { fullWidth -> fullWidth },
        animationSpec = tween(200, easing = LinearEasing)
    ) + fadeOut(targetAlpha = 0f, animationSpec = tween(200, easing = LinearEasing))
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun AppNavHost(
    onClickFloatingActionButton: () -> Unit,
    onClickExportActivityFile: (ActivityModel) -> Unit,
    openRoutePlanningAction: () -> Unit
) = ProvideWindowInsets {
    val navController = rememberAnimatedNavController()
    AnimatedNavHost(
        navController = navController,
        startDestination = HomeNavDestination.Home.route
    ) {
        addHomeDestination(
            navController,
            onClickFloatingActionButton,
            onClickExportActivityFile,
            openRoutePlanningAction
        )

        addProfileDestination(navController)

        addActivityDetailDestination(
            onClickExportActivityFile,
            navController
        )
    }
}

@OptIn(ExperimentalAnimationApi::class)
private fun NavGraphBuilder.addProfileDestination(navController: NavHostController) {
    composable(
        route = HomeNavDestination.Profile.route,
        arguments = HomeNavDestination.Profile.arguments,
        enterTransition = { _, _ -> AppNavTransitionDefaults.enterTransition },
        popEnterTransition = { _, _ -> AppNavTransitionDefaults.popEnterTransition },
        popExitTransition = { _, _ -> AppNavTransitionDefaults.popExitTransition }
    ) { backStackEntry ->
        UserProfileScreen(navController, backStackEntry)
    }
}

@OptIn(ExperimentalAnimationApi::class)
private fun NavGraphBuilder.addHomeDestination(
    navController: NavHostController,
    onClickFloatingActionButton: () -> Unit,
    onClickExportActivityFile: (ActivityModel) -> Unit,
    openRoutePlanningAction: () -> Unit
) {
    composable(
        route = HomeNavDestination.Home.route,
        enterTransition = { _, _ -> AppNavTransitionDefaults.enterTransition },
        popEnterTransition = { _, _ -> AppNavTransitionDefaults.popEnterTransition },
        popExitTransition = { _, _ -> AppNavTransitionDefaults.popExitTransition }
    ) {
        HomeScreen(
            navController,
            onClickFloatingActionButton,
            onClickExportActivityFile,
            openRoutePlanningAction
        )
    }
}

@OptIn(ExperimentalAnimationApi::class)
private fun NavGraphBuilder.addActivityDetailDestination(
    onClickExportFile: (ActivityModel) -> Unit,
    navController: NavHostController,
) {
    composable(
        route = HomeNavDestination.ActivityDetail.route,
        arguments = HomeNavDestination.ActivityDetail.arguments,
        enterTransition = { _, _ -> AppNavTransitionDefaults.enterTransition },
        popEnterTransition = { _, _ -> AppNavTransitionDefaults.popEnterTransition },
        popExitTransition = { _, _ -> AppNavTransitionDefaults.popExitTransition }
    ) { navBackStackEntry ->
        ActivityDetailScreen(
            navController,
            navBackStackEntry,
            onClickExportFile = onClickExportFile
        )
    }
}
