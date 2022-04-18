package akio.apps.myrun.feature.core.navigation

import androidx.navigation.NamedNavArgument
import androidx.navigation.NavArgumentBuilder
import androidx.navigation.NavBackStackEntry
import androidx.navigation.navArgument

abstract class NamedNavArgumentAdapter(
    open val namedArgument: NamedNavArgument,
) {
    open fun parseValueInBackStackEntry(entry: NavBackStackEntry): String? =
        entry.arguments?.getString(namedArgument.name)

    fun toNavArgumentValuePair(
        value: Any? = namedArgument.argument.defaultValue,
    ): NavArgumentValuePair = NavArgumentValuePair(namedArgument, value)
}

class OptionalNamedNavArgument(
    name: String,
    builder: NavArgumentBuilder.() -> Unit,
) : NamedNavArgumentAdapter(
    navArgument(name) {
        this.builder()
        nullable = true
    }
)

class RequiredNamedNavArgument(
    name: String,
    builder: NavArgumentBuilder.() -> Unit,
) : NamedNavArgumentAdapter(
    navArgument(name) {
        this.builder()
        nullable = false
    }
) {
    override fun parseValueInBackStackEntry(entry: NavBackStackEntry): String =
        super.parseValueInBackStackEntry(entry) ?: ""
}
