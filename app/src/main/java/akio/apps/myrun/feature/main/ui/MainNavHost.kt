package akio.apps.myrun.feature.main.ui

import akio.apps.myrun.data.activity.api.model.BaseActivityModel
import akio.apps.myrun.feature.activitydetail.ui.ActivityDetailScreen
import akio.apps.myrun.feature.core.navigation.HomeNavDestination
import akio.apps.myrun.feature.profile.ui.UserProfileScreen
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.google.accompanist.navigation.animation.composable
import com.google.accompanist.navigation.animation.rememberAnimatedNavController

private object AppNavTransitionDefaults {
    val enterTransition: EnterTransition = slideInHorizontally(
        initialOffsetX = { fullWidth -> fullWidth / 2 },
        animationSpec = tween(200)
    ) + fadeIn(initialAlpha = 0f, animationSpec = tween(200))

    val exitTransition: ExitTransition = fadeOut(
        targetAlpha = 1f,
        animationSpec = tween(200)
    )

    val popEnterTransition: EnterTransition = fadeIn(
        initialAlpha = 1f,
        animationSpec = tween(0)
    )

    val popExitTransition: ExitTransition = slideOutHorizontally(
        targetOffsetX = { fullWidth -> fullWidth },
        animationSpec = tween(200)
    ) + fadeOut(targetAlpha = 0f, animationSpec = tween(200))
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun MainNavHost(
    onClickFloatingActionButton: () -> Unit,
    onClickExportActivityFile: (BaseActivityModel) -> Unit,
    openRoutePlanningAction: () -> Unit,
) {
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
        enterTransition = { AppNavTransitionDefaults.enterTransition },
        exitTransition = { AppNavTransitionDefaults.exitTransition },
        popEnterTransition = { AppNavTransitionDefaults.popEnterTransition },
        popExitTransition = { AppNavTransitionDefaults.popExitTransition }
    ) { backStackEntry ->
        UserProfileScreen(navController, backStackEntry)
    }
}

@OptIn(ExperimentalAnimationApi::class)
private fun NavGraphBuilder.addHomeDestination(
    navController: NavHostController,
    onClickFloatingActionButton: () -> Unit,
    onClickExportActivityFile: (BaseActivityModel) -> Unit,
    openRoutePlanningAction: () -> Unit,
) {
    composable(
        route = HomeNavDestination.Home.route,
        enterTransition = { AppNavTransitionDefaults.enterTransition },
        exitTransition = { AppNavTransitionDefaults.exitTransition },
        popEnterTransition = { AppNavTransitionDefaults.popEnterTransition },
        popExitTransition = { AppNavTransitionDefaults.popExitTransition }
    ) { backStackEntry ->
        HomeTabScreen(
            navController,
            backStackEntry,
            onClickFloatingActionButton,
            onClickExportActivityFile,
            openRoutePlanningAction
        )
    }
}

@OptIn(ExperimentalAnimationApi::class)
private fun NavGraphBuilder.addActivityDetailDestination(
    onClickExportFile: (BaseActivityModel) -> Unit,
    navController: NavHostController,
) {
    composable(
        route = HomeNavDestination.ActivityDetail.route,
        arguments = HomeNavDestination.ActivityDetail.arguments,
        enterTransition = { AppNavTransitionDefaults.enterTransition },
        exitTransition = { AppNavTransitionDefaults.exitTransition },
        popEnterTransition = { AppNavTransitionDefaults.popEnterTransition },
        popExitTransition = { AppNavTransitionDefaults.popExitTransition }
    ) { navBackStackEntry ->
        ActivityDetailScreen(
            navController,
            navBackStackEntry,
            onClickExportFile = onClickExportFile
        )
    }
}
