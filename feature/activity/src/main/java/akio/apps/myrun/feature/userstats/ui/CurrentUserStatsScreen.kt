package akio.apps.myrun.feature.userstats.ui

import akio.apps.myrun.feature.core.ktx.rememberViewModelProvider
import akio.apps.myrun.feature.core.ui.AppTheme
import akio.apps.myrun.feature.userstats.UserStatsViewModel
import akio.apps.myrun.feature.userstats.di.DaggerUserStatsFeatureComponent
import android.app.Application
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController

@Composable
fun CurrentUserStatsScreen(
    appNavController: NavController,
    navEntry: NavBackStackEntry,
    contentPadding: PaddingValues = PaddingValues(),
    openRoutePlanningAction: () -> Unit,
) = AppTheme {
    val application = LocalContext.current.applicationContext as Application
    val userStatsViewModel = navEntry.rememberViewModelProvider { savedStateHandle ->
        DaggerUserStatsFeatureComponent.factory().create(
            application,
            UserStatsViewModel.initSavedState(savedStateHandle, userId = null)
        ).userStatsViewModel()
    }
    UserStatsScreen(userStatsViewModel, contentPadding, appNavController, openRoutePlanningAction)
}