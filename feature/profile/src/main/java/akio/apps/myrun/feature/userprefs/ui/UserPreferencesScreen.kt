package akio.apps.myrun.feature.userprefs.ui

import akio.apps.myrun.data.eapps.api.model.ExternalAppToken
import akio.apps.myrun.data.eapps.api.model.ExternalProviders
import akio.apps.myrun.data.eapps.api.model.ProviderToken
import akio.apps.myrun.data.eapps.api.model.RunningApp
import akio.apps.myrun.data.eapps.api.model.StravaAthlete
import akio.apps.myrun.data.user.api.model.MeasureSystem
import akio.apps.myrun.feature.core.ktx.rememberViewModelProvider
import akio.apps.myrun.feature.core.ui.AppBarArrowBackButton
import akio.apps.myrun.feature.core.ui.AppDimensions
import akio.apps.myrun.feature.core.ui.AppTheme
import akio.apps.myrun.feature.core.ui.CompoundSwitch
import akio.apps.myrun.feature.core.ui.CompoundText
import akio.apps.myrun.feature.core.ui.ConfirmationDialog
import akio.apps.myrun.feature.core.ui.ErrorDialog
import akio.apps.myrun.feature.core.ui.FormSectionSpace
import akio.apps.myrun.feature.core.ui.ProgressDialog
import akio.apps.myrun.feature.core.ui.SingleChoiceListDialog
import akio.apps.myrun.feature.core.ui.StatusBarSpacer
import akio.apps.myrun.feature.core.ui.addCompoundComponentPaddings
import akio.apps.myrun.feature.profile.LinkStravaDelegate
import akio.apps.myrun.feature.profile.R
import akio.apps.myrun.feature.profile.di.DaggerUserProfileFeatureComponent
import akio.apps.myrun.feature.userprefs.UserPreferencesViewModel
import android.app.Application
import android.content.Context
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.Checkbox
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CloudSync
import androidx.compose.material.icons.rounded.DeleteForever
import androidx.compose.material.icons.rounded.Error
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.SquareFoot
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController

@Composable
fun UserPreferencesScreen(
    navController: NavController,
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
    navController: NavController,
    userPrefsViewModel: UserPreferencesViewModel,
) = AppTheme {
    val preferredSystem by userPrefsViewModel.preferredSystem
        .collectAsState(initial = MeasureSystem.Default)
    val stravaLinkState by userPrefsViewModel.stravaLinkState
        .collectAsState(initial = UserPreferencesViewModel.StravaLinkState.Unknown)
    Column(modifier = Modifier.fillMaxSize()) {
        StatusBarSpacer()
        TopAppBar(
            navigationIcon = { AppBarArrowBackButton(navController) },
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

        FormSectionSpace()

        DeleteAccountSection()
    }

    // overlay things
    val isInProgress by userPrefsViewModel.isLaunchCatchingInProgress.collectAsState()
    val error by userPrefsViewModel.launchCatchingError.collectAsState()
    if (isInProgress) {
        ProgressDialog(stringResource(id = R.string.message_loading))
    }

    error.getContentIfNotHandled()?.let { ex ->
        ErrorDialog(ex.message ?: stringResource(id = R.string.dialog_delegate_unknown_error))
    }
}

@Composable
private fun DeleteAccountSection() {
    var isShowingConfirmDialog by remember { mutableStateOf(false) }
    CompoundText(
        label = stringResource(id = R.string.user_prefs_delete_account_label),
        icon = Icons.Rounded.DeleteForever,
        tint = Color.Red
    ) {
        isShowingConfirmDialog = true
    }

    if (isShowingConfirmDialog) {
        var isConfirmChecked by remember { mutableStateOf(false) }
        Dialog(onDismissRequest = { isShowingConfirmDialog = false }) {
            Surface(shape = MaterialTheme.shapes.medium) {
                Column(modifier = Modifier.padding(vertical = AppDimensions.rowVerticalPadding)) {
                    Icon(
                        Icons.Rounded.ErrorOutline,
                        null,
                        modifier = Modifier
                            .size(48.dp)
                            .align(Alignment.CenterHorizontally),
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier
                            .clickable { isConfirmChecked = !isConfirmChecked }
                            .addCompoundComponentPaddings(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(checked = isConfirmChecked, onCheckedChange = null)
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(stringResource(R.string.user_prefs_delete_account_confirm_message))
                    }
                    Row(
                        horizontalArrangement = Arrangement.End,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = AppDimensions.screenHorizontalPadding)
                    ) {
                        TextButton(onClick = { isShowingConfirmDialog = false }) {
                            Text(text = stringResource(id = R.string.action_cancel))
                        }
                        TextButton(onClick = { /*TODO*/ }) {
                            Text(text = stringResource(id = R.string.action_ok))
                        }
                    }
                }
            }
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
    val sectionLabel = stringResource(id = R.string.user_prefs_unit_of_measure_label)
    CompoundText(
        label = sectionLabel,
        value = preferredSystem.name,
        icon = Icons.Rounded.SquareFoot
    ) {
        isShowingListDialog = true
    }

    if (isShowingListDialog) {
        SingleChoiceListDialog(
            sectionLabel,
            items = allItems.map { it.name },
            preferredSystem.name,
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
        checked = linkState == UserPreferencesViewModel.StravaLinkState.Linked,
        startIcon = Icons.Rounded.CloudSync
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
            title = stringResource(id = R.string.user_prefs_app_strava_unlink_confirm_title),
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
