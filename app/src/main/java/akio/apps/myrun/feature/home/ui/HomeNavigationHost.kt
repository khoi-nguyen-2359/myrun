package akio.apps.myrun.feature.home.ui

import akio.apps.common.feature.viewmodel.viewModelProvider
import akio.apps.myrun.feature.home._di.DaggerHomeFeatureComponent
import akio.apps.myrun.feature.profile.DaggerUserProfileFeatureComponent
import akio.apps.myrun.feature.profile.ui.UserProfileScreen
import akio.apps.myrun.feature.usertimeline.model.Activity
import androidx.compose.runtime.Composable
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.accompanist.insets.ProvideWindowInsets

@Composable
fun HomeNavigationHost(
    onClickFloatingActionButton: () -> Unit,
    onClickActivityItemAction: (Activity) -> Unit,
    onClickExportActivityFile: (Activity) -> Unit,
) = ProvideWindowInsets {
    val navController = rememberNavController()
    NavHost(
        navController = navController,
        startDestination = MainNavigationDestination.Home.route
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

private fun NavGraphBuilder.addProfileDestination(navController: NavHostController) {
    composable(
        route = MainNavigationDestination.Profile.routeWithArguments,
        arguments = MainNavigationDestination.Profile.arguments
    ) { backStackEntry ->
        val arguments = MainNavigationDestination.Profile.parseArguments(backStackEntry)
        val userProfileViewModel = backStackEntry.viewModelProvider {
            DaggerUserProfileFeatureComponent.factory().create(arguments).userProfileViewModel()
        }
        UserProfileScreen(navController, userProfileViewModel)
    }
}

private fun NavGraphBuilder.addHomeDestination(
    onClickFloatingActionButton: () -> Unit,
    onClickActivityItemAction: (Activity) -> Unit,
    onClickExportActivityFile: (Activity) -> Unit,
    navController: NavHostController,
) {
    composable(MainNavigationDestination.Home.route) { backStackEntry ->
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
