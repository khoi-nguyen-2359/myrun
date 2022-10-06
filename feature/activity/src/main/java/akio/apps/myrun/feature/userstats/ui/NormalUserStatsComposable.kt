package akio.apps.myrun.feature.userstats.ui

import akio.apps.myrun.feature.core.navigation.HomeNavDestination
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController

@Composable
fun NormalUserStatsComposable(
    appNavController: NavController,
    navEntry: NavBackStackEntry,
    contentPaddings: PaddingValues = PaddingValues(),
    openRoutePlanningAction: () -> Unit = { },
) {
    val userId =
        HomeNavDestination.NormalUserStats.userIdRequiredArg.parseValueInBackStackEntry(navEntry)
    val arguments = UserStatsArguments(userId)
    UserStatsComposable(
        arguments,
        contentPaddings,
        appNavController,
        openRoutePlanningAction
    )
}
