package akio.apps.myrun.feature.home.ui

import akio.apps.myrun.feature.profile.UserProfileViewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavType
import androidx.navigation.compose.navArgument

sealed class MainNavigationDestination(open val route: String) {

    object Home : MainNavigationDestination(route = "home")

    object Profile : MainNavigationDestination(route = "profile") {
        private const val ARG_USER_ID = "userId"
        val routeWithArguments: String = "$route/{$ARG_USER_ID}"
        val arguments = listOf(navArgument(ARG_USER_ID) { type = NavType.StringType })

        fun parseArguments(backStackEntry: NavBackStackEntry): UserProfileViewModel.Arguments {
            val userId = backStackEntry.arguments?.getString(ARG_USER_ID)
            return UserProfileViewModel.Arguments(userId)
        }

        // Passing null [userId] to load current user
        fun routeWithUserId(userId: String?) = "$route/$userId"
    }
}
