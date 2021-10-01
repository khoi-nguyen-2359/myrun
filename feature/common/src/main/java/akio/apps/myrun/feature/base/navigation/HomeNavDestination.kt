package akio.apps.myrun.feature.base.navigation

import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavType
import androidx.navigation.compose.NamedNavArgument
import androidx.navigation.compose.navArgument

object HomeNavDestination {

    object Home : NavDestinationInfo(routeName = "home")

    object Profile : NavDestinationInfo(routeName = "profile") {
        private val userIdOptionalArg = navArgument("userId") {
            type = NavType.StringType
            defaultValue = null // null [userId] to load current user
            nullable = true
        }

        override val arguments: List<NamedNavArgument> = listOf(userIdOptionalArg)

        fun parseUserId(backStackEntry: NavBackStackEntry): String? =
            backStackEntry.arguments?.getString(userIdOptionalArg.name)

        fun routeWithUserId(userId: String? = null) = createRouteFromArguments(
            shouldIncludeValue = true,
            listOf(NavArgumentValuePair(userIdOptionalArg, userId))
        )
    }

    object ActivityDetail : NavDestinationInfo(routeName = "activity") {
        private val activityIdRequiredArg: NamedNavArgument = navArgument("activityId") {
            type = NavType.StringType
        }

        override val arguments: List<NamedNavArgument> = listOf(activityIdRequiredArg)

        fun parseActivityId(navBackStackEntry: NavBackStackEntry): String =
            navBackStackEntry.arguments?.getString(activityIdRequiredArg.name) ?: ""

        fun routeWithActivityId(activityId: String): String = createRouteFromArguments(
            shouldIncludeValue = true,
            listOf(NavArgumentValuePair(activityIdRequiredArg, activityId))
        )
    }
}
