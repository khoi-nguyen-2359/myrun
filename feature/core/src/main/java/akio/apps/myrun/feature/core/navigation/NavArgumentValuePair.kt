package akio.apps.myrun.feature.core.navigation

import androidx.navigation.NamedNavArgument

class NavArgumentValuePair(
    val argument: NamedNavArgument,
    val value: Any? = argument.argument.defaultValue,
)
