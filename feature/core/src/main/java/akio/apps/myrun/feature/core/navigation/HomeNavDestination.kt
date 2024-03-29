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

    object WebViewContainer : NavDestinationInfo(routeName = "webViewContainer") {
        val titleOptionalArg: OptionalNamedNavArgument =
            OptionalNamedNavArgument("title") { type = NavType.StringType }

        val urlRequiredArg: OptionalNamedNavArgument =
            OptionalNamedNavArgument("url") { type = NavType.StringType }

        override val argumentAdapters: List<NamedNavArgumentAdapter> =
            listOf(titleOptionalArg, urlRequiredArg)

        fun routeWithArgs(url: String, title: String? = null) = createRouteFromArguments(
            shouldIncludeValue = true,
            listOf(
                titleOptionalArg.toNavArgumentValuePair(title),
                urlRequiredArg.toNavArgumentValuePair(url)
            )
        )
    }

    object UserFollow : NavDestinationInfo(routeName = "userFollow") {
        private val userIdRequiredArg: RequiredNamedNavArgument =
            RequiredNamedNavArgument("userId") { type = NavType.StringType }

        override val argumentAdapters: List<NamedNavArgumentAdapter> = listOf(userIdRequiredArg)

        fun routeWithUserId(userId: String): String = createRouteFromArguments(
            shouldIncludeValue = true,
            listOf(userIdRequiredArg.toNavArgumentValuePair(userId))
        )
    }

    object NormalUserStats : NavDestinationInfo(routeName = "normalUserStats") {
        val userIdRequiredArg: RequiredNamedNavArgument = RequiredNamedNavArgument("userId") {
            type = NavType.StringType
        }
        override val argumentAdapters: List<NamedNavArgumentAdapter> = listOf(userIdRequiredArg)

        fun routeWithUserId(userId: String): String = createRouteFromArguments(
            shouldIncludeValue = true,
            listOf(userIdRequiredArg.toNavArgumentValuePair(userId))
        )
    }
}
