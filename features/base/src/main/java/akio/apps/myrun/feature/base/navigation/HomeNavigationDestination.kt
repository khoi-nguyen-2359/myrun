package akio.apps.myrun.feature.base.navigation

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

        fun parseUserId(backStackEntry: NavBackStackEntry): String? =
            backStackEntry.arguments?.getString(ARG_USER_ID)

        // Passing null [userId] to load current user
        fun routeWithUserId(userId: String?) =
            userId?.let { "$routeName?$ARG_USER_ID=$userId" } ?: routeName
    }

    object ActivityDetail : HomeNavigationDestination(routeName = "activity") {
        private const val ARG_ACTIVITY_ID = "activityId"

        fun parseActivityId(navBackStackEntry: NavBackStackEntry): String =
            navBackStackEntry.arguments?.getString(ARG_ACTIVITY_ID) ?: ""

        fun routeWithActivityId(activityId: String): String =
            "$routeName/$activityId"

        override val route: String = "$routeName/{$ARG_ACTIVITY_ID}"
        override val arguments: List<NamedNavArgument> = listOf(
            navArgument(ARG_ACTIVITY_ID) { type = NavType.StringType }
        )
    }
}
