package akio.apps.myrun.feature.base.navigation

import androidx.navigation.compose.NamedNavArgument

abstract class NavDestinationInfo(protected val routeName: String) {
    open val arguments: List<NamedNavArgument> = emptyList()
    val route: String by lazy {
        createRouteFromArguments(shouldIncludeValue = false, arguments.map(::NavArgumentValuePair))
    }

    /**
     * [shouldIncludeValue] - True to create route with values that present in the [arguments], used
     * to build route for actual navigating. Otherwise returns a route template to declare a
     * destination in navigation host.
     */
    protected fun createRouteFromArguments(
        shouldIncludeValue: Boolean,
        arguments: List<NavArgumentValuePair>,
    ): String {
        val optionalArgsBuilder = StringBuilder()
        val requiredArgsBuilder = StringBuilder()
        arguments.forEach { argValuePair ->
            val (name, argument) = argValuePair.argument
            val value = argValuePair.value
            if (argument.isDefaultValuePresent || argument.isNullable) {
                if (shouldIncludeValue) {
                    if (value != null) {
                        optionalArgsBuilder.append("$name=$value&")
                    }
                } else {
                    optionalArgsBuilder.append("$name={$name}&")
                }
            } else {
                if (shouldIncludeValue) {
                    requiredArgsBuilder.append("$value/")
                } else {
                    requiredArgsBuilder.append("{$name}/")
                }
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

    class NavArgumentValuePair(
        val argument: NamedNavArgument,
        val value: Any? = argument.argument.defaultValue
    )
}
