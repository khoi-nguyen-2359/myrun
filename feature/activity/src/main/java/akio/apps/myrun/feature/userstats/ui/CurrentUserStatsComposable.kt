package akio.apps.myrun.feature.userstats.ui

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.navigation.NavController

@Composable
fun CurrentUserStatsComposable(
    appNavController: NavController,
    contentPadding: PaddingValues = PaddingValues(),
    openRoutePlanningAction: () -> Unit,
) = UserStatsComposable(
    UserStatsArguments(userId = null),
    contentPadding,
    appNavController,
    openRoutePlanningAction
)
