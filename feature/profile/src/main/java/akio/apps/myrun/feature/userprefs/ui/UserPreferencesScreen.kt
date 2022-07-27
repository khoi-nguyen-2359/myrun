package akio.apps.myrun.feature.userprefs.ui

import akio.apps.myrun.feature.core.ui.AppBarIconButton
import akio.apps.myrun.feature.core.ui.AppTheme
import akio.apps.myrun.feature.core.ui.CompoundSwitch
import akio.apps.myrun.feature.core.ui.CompoundText
import akio.apps.myrun.feature.core.ui.FormSectionSpace
import akio.apps.myrun.feature.core.ui.StatusBarSpacer
import akio.apps.myrun.feature.profile.R
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController

@Composable
fun UserPreferencesScreen(
    navController: NavHostController,
    backStackEntry: NavBackStackEntry,
) = AppTheme {
    Column(modifier = Modifier.fillMaxSize()) {
        StatusBarSpacer()
        TopAppBar(
            navigationIcon = {
                AppBarIconButton(iconImageVector = Icons.Rounded.ArrowBack) {
                    navController.popBackStack()
                }
            },
            title = { Text(text = stringResource(id = R.string.user_prefs_title)) }
        )

        FormSectionSpace()

        CompoundText(
            label = stringResource(id = R.string.user_prefs_unit_of_measure_label),
            value = "Metric"
        )

        FormSectionSpace()

        CompoundSwitch(
            label = stringResource(id = R.string.user_profile_strava_description),
            checked = true,
            enabled = true
        ) {
        }
    }
}
