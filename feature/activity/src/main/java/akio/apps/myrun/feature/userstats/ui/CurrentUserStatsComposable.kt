package akio.apps.myrun.feature.userstats.ui

import akio.apps.myrun.feature.userstats.UserStatsViewModel
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@Composable
fun CurrentUserStatsComposable(
    appNavController: NavController,
    contentPaddingBottom: Dp = 0.dp,
    openRoutePlanningAction: () -> Unit,
) = UserStatsComposable(
    UserStatsViewModel.UserStatsArguments(userId = null),
    contentPaddingBottom,
    appNavController,
    openRoutePlanningAction
)
