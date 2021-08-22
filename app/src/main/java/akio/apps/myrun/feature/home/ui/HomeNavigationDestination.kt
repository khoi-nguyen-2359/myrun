package akio.apps.myrun.feature.home.ui

import akio.apps.myrun.feature.profile.UserProfileViewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavType
import androidx.navigation.compose.NamedNavArgument
import androidx.navigation.compose.navArgument

sealed class HomeNavigationDestination(protected val routeName: String) {

    open val route: String = routeName
    open val arguments: List<NamedNavArgument> = emptyList()

    object Home : HomeNavigationDestination(routeName = "home")

    object Profile : HomeNavigationDestination(routeName = "profile") {
        private const val ARG_USER_ID = "userId"

        override val route: String = "$routeName?$ARG_USER_ID={$ARG_USER_ID}"
        override val arguments: List<NamedNavArgument> = listOf(
            navArgument(ARG_USER_ID) {
                type = NavType.StringType
                defaultValue = null
                nullable = true
            }
        )

        fun parseArguments(backStackEntry: NavBackStackEntry): UserProfileViewModel.Arguments {
            val userId = backStackEntry.arguments?.getString(ARG_USER_ID)
            return UserProfileViewModel.Arguments(userId)
        }

        // Passing null [userId] to load current user
        fun routeWithUserId(userId: String?) =
            userId?.let { "$routeName?$ARG_USER_ID=$userId" } ?: routeName
    }
}
