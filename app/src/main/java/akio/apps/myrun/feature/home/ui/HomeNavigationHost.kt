package akio.apps.myrun.feature.home.ui

import akio.apps.common.feature.viewmodel.savedStateViewModelProvider
import akio.apps.common.feature.viewmodel.viewModelProvider
import akio.apps.myrun.data.activity.api.model.ActivityModel
import akio.apps.myrun.feature.activitydetail.ActivityDetailViewModel
import akio.apps.myrun.feature.activitydetail.DaggerActivityDetailFeatureComponent
import akio.apps.myrun.feature.activitydetail.ui.ActivityDetailScreen
import akio.apps.myrun.feature.base.navigation.HomeNavigationDestination
import akio.apps.myrun.feature.home._di.DaggerHomeFeatureComponent
import akio.apps.myrun.feature.profile.DaggerUserProfileFeatureComponent
import akio.apps.myrun.feature.profile.UserProfileViewModel
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
object HomeNavigationTransitionDefaults {
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
fun HomeNavigationHost(
    onClickFloatingActionButton: () -> Unit,
    onClickExportActivityFile: (ActivityModel) -> Unit,
) = ProvideWindowInsets {
    val navController = rememberAnimatedNavController()
    AnimatedNavHost(
        navController = navController,
        startDestination = HomeNavigationDestination.Home.route
    ) {
        addHomeDestination(
            onClickFloatingActionButton,
            onClickExportActivityFile,
            navController
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
        route = HomeNavigationDestination.Profile.route,
        arguments = HomeNavigationDestination.Profile.arguments,
        enterTransition = { _, _ -> HomeNavigationTransitionDefaults.enterTransition },
        popEnterTransition = { _, _ -> HomeNavigationTransitionDefaults.popEnterTransition },
        popExitTransition = { _, _ -> HomeNavigationTransitionDefaults.popExitTransition }
    ) { backStackEntry ->
        val userId = HomeNavigationDestination.Profile.parseUserId(backStackEntry)
        val userProfileViewModel = backStackEntry.savedStateViewModelProvider(
            backStackEntry
        ) { handle ->
            DaggerUserProfileFeatureComponent.factory()
                .create(UserProfileViewModel.setInitialSavedState(handle, userId))
                .userProfileViewModel()
        }
        UserProfileScreen(navController, userProfileViewModel)
    }
}

@OptIn(ExperimentalAnimationApi::class)
private fun NavGraphBuilder.addHomeDestination(
    onClickFloatingActionButton: () -> Unit,
    onClickExportActivityFile: (ActivityModel) -> Unit,
    navController: NavHostController,
) {
    composable(
        route = HomeNavigationDestination.Home.route,
        enterTransition = { _, _ -> HomeNavigationTransitionDefaults.enterTransition },
        popEnterTransition = { _, _ -> HomeNavigationTransitionDefaults.popEnterTransition },
        popExitTransition = { _, _ -> HomeNavigationTransitionDefaults.popExitTransition }
    ) { backStackEntry ->
        val userFeedViewModel = backStackEntry.viewModelProvider {
            DaggerHomeFeatureComponent.factory().create().userFeedViewModel()
        }
        HomeScreen(
            onClickFloatingActionButton,
            onClickExportActivityFile,
            navController,
            userFeedViewModel
        )
    }
}

@OptIn(ExperimentalAnimationApi::class)
private fun NavGraphBuilder.addActivityDetailDestination(
    onClickExportFile: (ActivityModel) -> Unit,
    navController: NavHostController,
) {
    composable(
        route = HomeNavigationDestination.ActivityDetail.route,
        arguments = HomeNavigationDestination.ActivityDetail.arguments,
        enterTransition = { _, _ -> HomeNavigationTransitionDefaults.enterTransition },
        popEnterTransition = { _, _ -> HomeNavigationTransitionDefaults.popEnterTransition },
        popExitTransition = { _, _ -> HomeNavigationTransitionDefaults.popExitTransition }
    ) { navBackStackEntry ->
        val activityId = HomeNavigationDestination.ActivityDetail.parseActivityId(navBackStackEntry)
        val activityDetailViewModel = navBackStackEntry.savedStateViewModelProvider(
            navBackStackEntry
        ) { handle ->
            DaggerActivityDetailFeatureComponent.factory()
                .create(ActivityDetailViewModel.setInitialSavedState(handle, activityId))
                .activityDetailsViewModel()
        }
        ActivityDetailScreen(
            activityDetailViewModel = activityDetailViewModel,
            onClickExportFile = onClickExportFile,
            navController
        )
    }
}
