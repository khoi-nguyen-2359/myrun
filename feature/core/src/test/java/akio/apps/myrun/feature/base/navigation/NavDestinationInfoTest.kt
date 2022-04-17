package akio.apps.myrun.feature.base.navigation

import akio.apps.myrun.feature.core.navigation.NamedNavArgumentAdapter
import akio.apps.myrun.feature.core.navigation.NavDestinationInfo
import akio.apps.myrun.feature.core.navigation.OptionalNamedNavArgument
import akio.apps.myrun.feature.core.navigation.RequiredNamedNavArgument
import android.os.Bundle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavType
import kotlin.test.assertEquals
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class NavDestinationInfoTest {
    private lateinit var navDestinationInfo: NavDestinationInfo

    @Test
    fun getRouteWithRequiredAndOptionalArguments() {
        navDestinationInfo = object : NavDestinationInfo(routeName = "defaultRouteName") {
            override val argumentAdapters: List<NamedNavArgumentAdapter> = listOf(
                RequiredNamedNavArgument("required_arg01") {
                    type = NavType.StringType
                },
                OptionalNamedNavArgument("optional_arg01") {
                    type = NavType.StringType
                },
                RequiredNamedNavArgument("required_arg02") {
                    type = NavType.StringType
                },
                OptionalNamedNavArgument("optional_arg02") {
                    type = NavType.StringType
                    defaultValue = "arg02"
                }
            )
        }

        assertEquals(
            "defaultRouteName" +
                "/{required_arg01}" +
                "/{required_arg02}" +
                "?optional_arg01={optional_arg01}" +
                "&optional_arg02={optional_arg02}",
            navDestinationInfo.route
        )
    }

    @Test
    fun getRouteWithoutArguments() {
        navDestinationInfo = object : NavDestinationInfo(routeName = "defaultRouteName") {
            override val argumentAdapters: List<NamedNavArgumentAdapter> = emptyList()
        }

        assertEquals("defaultRouteName", navDestinationInfo.route)
    }

    @Test
    fun testRequiredNamedNavArgument_parseValueInBackStackEntry() {
        val arg = RequiredNamedNavArgument("required_arg") {
            nullable = true // to test whether this value is ignored for required argument
        }
        val argumentBundle = mock<Bundle> {
            on { getString("required_arg") }.thenReturn("required_arg_value")
        }
        val navBackStackEntry = mock<NavBackStackEntry>()
        whenever(navBackStackEntry.arguments).thenReturn(argumentBundle)
        val parsedValue = arg.parseValueInBackStackEntry(navBackStackEntry)
        assertEquals("required_arg_value", parsedValue)
        assertEquals(false, arg.namedArgument.argument.isNullable)
    }

    @Test
    fun testOptionalNamedNavArgument_parseValueInBackStackEntry() {
        val arg = OptionalNamedNavArgument("optional_arg") {
            nullable = false // to test whether this value is ignored for required argument
        }
        val argumentBundle = mock<Bundle> {
            on { getString("optional_arg") }.thenReturn(null)
        }
        val navBackStackEntry = mock<NavBackStackEntry>()
        whenever(navBackStackEntry.arguments).thenReturn(argumentBundle)
        val parsedValue = arg.parseValueInBackStackEntry(navBackStackEntry)
        assertEquals(null, parsedValue)
        assertEquals(true, arg.namedArgument.argument.isNullable)
    }
}
