package akio.apps.myrun.feature.home.ui

import akio.apps.common.feature.viewmodel.viewModelProvider
import akio.apps.myrun.feature.home._di.DaggerHomeFeatureComponent
import akio.apps.myrun.feature.profile.DaggerUserProfileFeatureComponent
import akio.apps.myrun.feature.profile.ui.UserProfileScreen
import akio.apps.myrun.feature.usertimeline.model.Activity
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
@Composable
fun HomeNavigationHost(
    onClickFloatingActionButton: () -> Unit,
    onClickActivityItemAction: (Activity) -> Unit,
    onClickExportActivityFile: (Activity) -> Unit,
) = ProvideWindowInsets {
    val navController = rememberAnimatedNavController()
    AnimatedNavHost(
        navController = navController,
        startDestination = HomeNavigationDestination.Home.route
    ) {
        addHomeDestination(
            onClickFloatingActionButton,
            onClickActivityItemAction,
            onClickExportActivityFile,
            navController
        )

        addProfileDestination(navController)
    }
}

@OptIn(ExperimentalAnimationApi::class)
private fun NavGraphBuilder.addProfileDestination(navController: NavHostController) {
    composable(
        route = HomeNavigationDestination.Profile.route,
        arguments = HomeNavigationDestination.Profile.arguments,
        enterTransition = { _, _ ->
            slideInHorizontally(
                initialOffsetX = { fullWidth -> fullWidth / 2 },
                animationSpec = tween(200, easing = LinearEasing)
            ) + fadeIn(initialAlpha = 0f, animationSpec = tween(200, easing = LinearEasing))
        },
        popExitTransition = { _, _ ->
            slideOutHorizontally(
                targetOffsetX = { fullWidth -> fullWidth },
                animationSpec = tween(200, easing = LinearEasing)
            ) + fadeOut(targetAlpha = 0f, animationSpec = tween(200, easing = LinearEasing))
        }
    ) { backStackEntry ->
        val arguments = HomeNavigationDestination.Profile.parseArguments(backStackEntry)
        val userProfileViewModel = backStackEntry.viewModelProvider {
            DaggerUserProfileFeatureComponent.factory().create(arguments).userProfileViewModel()
        }
        UserProfileScreen(navController, userProfileViewModel)
    }
}

@OptIn(ExperimentalAnimationApi::class)
private fun NavGraphBuilder.addHomeDestination(
    onClickFloatingActionButton: () -> Unit,
    onClickActivityItemAction: (Activity) -> Unit,
    onClickExportActivityFile: (Activity) -> Unit,
    navController: NavHostController,
) {
    composable(
        route = HomeNavigationDestination.Home.route,
        popEnterTransition = { _, _ ->
            slideInHorizontally(
                initialOffsetX = { 0 },
                animationSpec = tween(200, easing = LinearEasing)
            ) + fadeIn(initialAlpha = 1f, animationSpec = tween(200, easing = LinearEasing))
        },
        popExitTransition = { _, _ ->
            slideOutHorizontally(
                targetOffsetX = { fullWidth -> fullWidth },
                animationSpec = tween(200, easing = LinearEasing)
            ) + fadeOut(targetAlpha = 0f, animationSpec = tween(200, easing = LinearEasing))
        }
    ) { backStackEntry ->
        val userFeedViewModel = backStackEntry.viewModelProvider {
            DaggerHomeFeatureComponent.factory().create().userFeedViewModel()
        }
        HomeScreen(
            onClickFloatingActionButton,
            onClickActivityItemAction,
            onClickExportActivityFile,
            navController,
            userFeedViewModel
        )
    }
}
