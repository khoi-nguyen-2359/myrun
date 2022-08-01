package akio.apps.myrun.feature.core.navigation

import androidx.navigation.NavType

object HomeNavDestination {

    object Home : NavDestinationInfo(routeName = "home")

    object Profile : NavDestinationInfo(routeName = "profile") {
        val userIdOptionalArg: OptionalNamedNavArgument = OptionalNamedNavArgument("userId") {
            type = NavType.StringType
            defaultValue = null // null [userId] to load current user
        }

        override val argumentAdapters: List<NamedNavArgumentAdapter> = listOf(userIdOptionalArg)

        fun routeWithUserId(userId: String? = null) = createRouteFromArguments(
            shouldIncludeValue = true,
            listOf(userIdOptionalArg.toNavArgumentValuePair(userId))
        )
    }

    object ActivityDetail : NavDestinationInfo(routeName = "activity") {
        val activityIdRequiredArg: RequiredNamedNavArgument =
            RequiredNamedNavArgument("activityId") {
                type = NavType.StringType
            }

        override val argumentAdapters: List<NamedNavArgumentAdapter> = listOf(activityIdRequiredArg)

        fun routeWithActivityId(activityId: String): String = createRouteFromArguments(
            shouldIncludeValue = true,
            listOf(activityIdRequiredArg.toNavArgumentValuePair(activityId))
        )
    }

    object UserPreferences : NavDestinationInfo(routeName = "userPreferences")
    object DeleteAccount : NavDestinationInfo(routeName = "deleteAccount")
}
