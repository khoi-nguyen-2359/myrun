package akio.apps.myrun.feature.userprefs.ui

import akio.apps.myrun.data.eapps.api.model.ExternalAppToken
import akio.apps.myrun.data.eapps.api.model.ExternalProviders
import akio.apps.myrun.data.eapps.api.model.ProviderToken
import akio.apps.myrun.data.eapps.api.model.RunningApp
import akio.apps.myrun.data.eapps.api.model.StravaAthlete
import akio.apps.myrun.data.user.api.model.MeasureSystem
import akio.apps.myrun.feature.core.ktx.rememberViewModelProvider
import akio.apps.myrun.feature.core.ui.AppBarIconButton
import akio.apps.myrun.feature.core.ui.AppTheme
import akio.apps.myrun.feature.core.ui.CompoundSwitch
import akio.apps.myrun.feature.core.ui.CompoundText
import akio.apps.myrun.feature.core.ui.ConfirmationDialog
import akio.apps.myrun.feature.core.ui.FormSectionSpace
import akio.apps.myrun.feature.core.ui.ListDialog
import akio.apps.myrun.feature.core.ui.StatusBarSpacer
import akio.apps.myrun.feature.profile.LinkStravaDelegate
import akio.apps.myrun.feature.profile.R
import akio.apps.myrun.feature.profile.di.DaggerUserProfileFeatureComponent
import akio.apps.myrun.feature.userprefs.UserPreferencesViewModel
import android.app.Application
import android.content.Context
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController

@Composable
fun UserPreferencesScreen(
    navController: NavHostController,
    backStackEntry: NavBackStackEntry,
) {
    val application = LocalContext.current.applicationContext as Application
    val viewModel = backStackEntry.rememberViewModelProvider {
        DaggerUserProfileFeatureComponent.factory().create(application, it).userPrefsViewModel()
    }
    UserPreferencesScreen(navController, viewModel)
}

@Composable
private fun UserPreferencesScreen(
    navController: NavHostController,
    userPrefsViewModel: UserPreferencesViewModel,
) = AppTheme {
    val preferredSystem by userPrefsViewModel.preferredSystem
        .collectAsState(initial = MeasureSystem.Default)
    val stravaLinkState by userPrefsViewModel.stravaLinkState
        .collectAsState(initial = UserPreferencesViewModel.StravaLinkState.Unknown)
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

        MeasureSystemSection(preferredSystem) { measureSystem ->
            userPrefsViewModel.selectMeasureSystem(measureSystem)
        }

        FormSectionSpace()

        StravaLinkSwitch(stravaLinkState) {
            userPrefsViewModel.deauthorizeStrava()
        }
    }
}

@Composable
private fun MeasureSystemSection(
    preferredSystem: MeasureSystem,
    onSelectMeasureSystem: (MeasureSystem) -> Unit,
) {
    var isShowingListDialog by remember { mutableStateOf(false) }
    val allItems = MeasureSystem.values()
    CompoundText(
        label = stringResource(id = R.string.user_prefs_unit_of_measure_label),
        value = preferredSystem.name
    ) {
        isShowingListDialog = true
    }

    if (isShowingListDialog) {
        ListDialog(
            items = allItems.map { it.name },
            onClickAtIndex = { index -> onSelectMeasureSystem(allItems[index]) },
            onDismiss = { isShowingListDialog = false }
        )
    }
}

@Composable
private fun StravaLinkSwitch(
    linkState: UserPreferencesViewModel.StravaLinkState,
    onConfirmToUnlinkStrava: () -> Unit,
) {
    var isUnlinkAlertShowing by remember { mutableStateOf(false) }
    val context = LocalContext.current
    CompoundSwitch(
        label = stringResource(id = R.string.user_profile_strava_description),
        enabled = linkState != UserPreferencesViewModel.StravaLinkState.Unknown,
        checked = linkState == UserPreferencesViewModel.StravaLinkState.Linked
    ) {
        when (linkState) {
            UserPreferencesViewModel.StravaLinkState.NotLinked ->
                openStravaLinkActivity(context)
            UserPreferencesViewModel.StravaLinkState.Linked ->
                isUnlinkAlertShowing = true
            else -> { /* do nothing */
            }
        }
    }

    if (isUnlinkAlertShowing) {
        ConfirmationDialog(
            onDismiss = { isUnlinkAlertShowing = false },
            confirmLabel = stringResource(id = R.string.action_yes),
            onConfirmed = { onConfirmToUnlinkStrava() },
            cancelLabel = stringResource(id = R.string.action_no),
            message = stringResource(id = R.string.user_prefs_app_strava_unlink_confirm_message)
        )
    }
}

private fun openStravaLinkActivity(context: Context) {
    val intent = LinkStravaDelegate.buildStravaLoginIntent(context)
    context.startActivity(intent)
}

private fun createExternalProviders() =
    ExternalProviders(
        ProviderToken(
            RunningApp.Strava,
            ExternalAppToken.StravaToken(
                "accessToken",
                "refreshToken",
                StravaAthlete(0L)
            )
        )
    )
