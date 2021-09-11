package akio.apps.myrun.feature.base.navigation

import androidx.navigation.NavType
import androidx.navigation.compose.navArgument
import kotlin.test.assertEquals
import org.junit.Test

class NavDestinationInfoTest {
    private lateinit var navDestinationInfo: NavDestinationInfo

    @Test
    fun getRouteWithRequiredAndOptionalArguments() {
        navDestinationInfo = object : NavDestinationInfo(
            routeName = "defaultRouteName",
            arguments = listOf(
                navArgument("required_arg01") {
                    type = NavType.StringType
                },
                navArgument("optional_arg01") {
                    type = NavType.StringType
                    nullable = true
                },
                navArgument("required_arg02") {
                    type = NavType.StringType
                },
                navArgument("optional_arg02") {
                    type = NavType.StringType
                    defaultValue = "arg02"
                }
            )
        ) {}

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
        navDestinationInfo = object : NavDestinationInfo(
            routeName = "defaultRouteName",
            arguments = emptyList()
        ) {}

        assertEquals("defaultRouteName", navDestinationInfo.route)
    }
}
