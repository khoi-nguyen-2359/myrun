package akio.apps.myrun.feature.base.navigation

import androidx.navigation.NamedNavArgument

class NavArgumentValuePair(
    val argument: NamedNavArgument,
    val value: Any? = argument.argument.defaultValue,
)
