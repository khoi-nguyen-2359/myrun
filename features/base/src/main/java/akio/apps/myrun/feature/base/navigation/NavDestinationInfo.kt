package akio.apps.myrun.feature.base.navigation

import androidx.navigation.compose.NamedNavArgument

abstract class NavDestinationInfo(
    protected val routeName: String,
    val arguments: List<NamedNavArgument> = emptyList()
) {
    val route: String = generateRouteFromArguments(arguments)

    private fun generateRouteFromArguments(arguments: List<NamedNavArgument>): String {
        val optionalArgsBuilder = StringBuilder()
        val requiredArgsBuilder = StringBuilder()
        arguments.forEach { (name, argument) ->
            if (argument.isDefaultValuePresent || argument.isNullable) {
                optionalArgsBuilder.append("$name={$name}&")
            } else {
                requiredArgsBuilder.append("{$name}/")
            }
        }
        val optionalArgsString = optionalArgsBuilder.removeSuffix("&")
        val requiredArgsString = requiredArgsBuilder.removeSuffix("/")

        val routeBuilder = StringBuilder(routeName)
        if (requiredArgsString.isNotEmpty()) {
            routeBuilder.append("/$requiredArgsString")
        }
        if (optionalArgsString.isNotEmpty()) {
            routeBuilder.append("?$optionalArgsString")
        }
        return routeBuilder.toString()
    }
}
