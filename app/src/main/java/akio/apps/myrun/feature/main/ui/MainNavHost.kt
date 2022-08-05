package akio.apps.myrun.feature.main.ui

import akio.apps.myrun.data.activity.api.model.BaseActivityModel
import akio.apps.myrun.feature.activitydetail.ui.ActivityDetailScreen
import akio.apps.myrun.feature.core.navigation.HomeNavDestination
import akio.apps.myrun.feature.profile.ui.UserProfileScreen
import akio.apps.myrun.feature.userprefs.ui.UserPreferencesScreen
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.google.accompanist.navigation.animation.composable
import com.google.accompanist.navigation.animation.rememberAnimatedNavController

private object AppNavTransitionDefaults {
    private const val duration = 200
    val enterTransition: EnterTransition = slideInHorizontally(
        initialOffsetX = { fullWidth -> fullWidth / 2 },
        animationSpec = tween(duration)
    ) + fadeIn(animationSpec = tween(duration))

    val exitTransition: ExitTransition = fadeOut(
        animationSpec = tween(duration)
    )

    val popEnterTransition: EnterTransition = fadeIn(
        animationSpec = tween(duration)
    )

    val popExitTransition: ExitTransition = slideOutHorizontally(
        targetOffsetX = { fullWidth -> fullWidth / 2 },
        animationSpec = tween(duration)
    ) + fadeOut(animationSpec = tween(duration))
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
        startDestination = HomeNavDestination.Home.route,
        enterTransition = { AppNavTransitionDefaults.enterTransition },
        exitTransition = { AppNavTransitionDefaults.exitTransition },
        popEnterTransition = { AppNavTransitionDefaults.popEnterTransition },
        popExitTransition = { AppNavTransitionDefaults.popExitTransition }
    ) {
        composable(HomeNavDestination.Home.route) { navEntry ->
            HomeTabScreen(
                navController,
                navEntry,
                onClickFloatingActionButton,
                onClickExportActivityFile,
                openRoutePlanningAction
            )
        }

        composable(
            route = HomeNavDestination.Profile.route,
            arguments = HomeNavDestination.Profile.arguments
        ) { navEntry ->
            UserProfileScreen(navController, navEntry)
        }

        composable(
            route = HomeNavDestination.ActivityDetail.route,
            arguments = HomeNavDestination.ActivityDetail.arguments
        ) { navEntry ->
            ActivityDetailScreen(navController, navEntry, onClickExportActivityFile)
        }

        composable(HomeNavDestination.UserPreferences.route) { navEntry ->
            UserPreferencesScreen(navController, navEntry)
        }
    }
}
