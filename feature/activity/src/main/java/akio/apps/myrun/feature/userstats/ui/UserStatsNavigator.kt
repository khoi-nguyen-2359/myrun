package akio.apps.myrun.feature.userstats.ui

import akio.apps.myrun.feature.core.navigation.HomeNavDestination
import androidx.navigation.NavController

class UserStatsNavigator(val navController: NavController) {
    fun navigateProfileScreen(userId: String) {
        navController.navigate(HomeNavDestination.Profile.routeWithUserId(userId))
    }

    fun navigateUserPreferencesScreen() {
        navController.navigate(HomeNavDestination.UserPreferences.route)
    }

    fun navigateUserFollowTab(userId: String) {
        val userFollowRoute = HomeNavDestination.UserFollow.routeWithUserId(userId)
        navController.navigate(userFollowRoute)
    }
}