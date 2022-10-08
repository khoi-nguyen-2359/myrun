package akio.apps.myrun.feature.main.ui

import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController

internal class HomeTabNavigator(
    val homeNavHostController: NavHostController,
    val currentTabNavEntry: NavBackStackEntry?,
) {
    fun isSelectedDestination(destination: HomeNavItemInfo) = currentTabNavEntry
        ?.destination
        ?.hierarchy
        ?.any { it.route == destination.route } == true

    fun navigateHomeDestination(destination: HomeNavItemInfo) {
        val currentEntryDesId = currentTabNavEntry?.destination?.id
            ?: homeNavHostController.graph.findStartDestination().id
        homeNavHostController.navigate(destination.route) {
            popUpTo(currentEntryDesId) {
                inclusive = true
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }
}
