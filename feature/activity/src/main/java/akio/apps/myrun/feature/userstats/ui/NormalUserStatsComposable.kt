package akio.apps.myrun.feature.userstats.ui

import akio.apps.myrun.feature.core.navigation.HomeNavDestination
import akio.apps.myrun.feature.userstats.UserStatsViewModel
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController

@Composable
fun NormalUserStatsComposable(
    appNavController: NavController,
    navEntry: NavBackStackEntry,
    openRoutePlanningAction: () -> Unit = { },
) {
    val userId =
        HomeNavDestination.NormalUserStats.userIdRequiredArg.parseValueInBackStackEntry(navEntry)
    val arguments = UserStatsViewModel.UserStatsArguments(userId)
    UserStatsComposable(
        arguments,
        contentPaddingBottom = 0.dp,
        appNavController,
        openRoutePlanningAction
    )
}
