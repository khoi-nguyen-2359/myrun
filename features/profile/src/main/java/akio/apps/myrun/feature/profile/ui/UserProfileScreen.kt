package akio.apps.myrun.feature.profile.ui

import akio.apps.common.data.Resource
import akio.apps.common.feature.ui.filterFloatTextField
import akio.apps.myrun.data.eapps.api.model.ExternalAppToken
import akio.apps.myrun.data.eapps.api.model.ExternalProviders
import akio.apps.myrun.data.eapps.api.model.ProviderToken
import akio.apps.myrun.data.eapps.api.model.RunningApp
import akio.apps.myrun.data.eapps.api.model.StravaAthlete
import akio.apps.myrun.data.user.api.model.Gender
import akio.apps.myrun.data.user.api.model.UserProfile
import akio.apps.myrun.feature.base.ui.AppBarIconButton
import akio.apps.myrun.feature.base.ui.AppBarTextButton
import akio.apps.myrun.feature.base.ui.AppColors
import akio.apps.myrun.feature.base.ui.AppDimensions
import akio.apps.myrun.feature.base.ui.AppTheme
import akio.apps.myrun.feature.base.ui.CentralAnnouncementView
import akio.apps.myrun.feature.base.ui.CentralLoadingView
import akio.apps.myrun.feature.base.ui.ErrorDialog
import akio.apps.myrun.feature.base.ui.NavigationBarSpacer
import akio.apps.myrun.feature.base.ui.ProgressDialog
import akio.apps.myrun.feature.base.ui.StatusBarSpacer
import akio.apps.myrun.feature.profile.LinkStravaDelegate
import akio.apps.myrun.feature.profile.R
import akio.apps.myrun.feature.profile.UploadAvatarActivity
import akio.apps.myrun.feature.profile.UserProfileViewModel
import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.AlertDialog
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material.ListItem
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Surface
import androidx.compose.material.Switch
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.sharp.ArrowBack
import androidx.compose.material.icons.sharp.PhotoCamera
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.annotation.ExperimentalCoilApi
import coil.compose.rememberImagePainter
import coil.size.Scale
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date

@Composable
fun UserProfileScreen(
    navController: NavController,
    userProfileViewModel: UserProfileViewModel,
) = AppTheme {
    val screenState by userProfileViewModel.userProfileScreenStateFlow
        .collectAsState(initial = UserProfileViewModel.UserProfileScreenState.Loading)
    UserProfileScreen(
        screenState,
        navController,
        onConfirmToUnlinkStrava = { userProfileViewModel.deauthorizeStrava() },
        onClickSaveUserProfile = {
            userProfileViewModel.updateUserProfile()
            navController.popBackStack()
        },
        onFormDataChanged = { editedFormData ->
            userProfileViewModel.onFormDataChanged(editedFormData)
        }
    )

    // overlay things
    val isInProgress by userProfileViewModel.isLaunchCatchingInProgress.collectAsState()
    val error by userProfileViewModel.launchCatchingError.collectAsState()
    if (isInProgress) {
        ProgressDialog(stringResource(id = R.string.message_loading))
    }

    error.getContentIfNotHandled()?.let { ex ->
        ErrorDialog(ex.message ?: stringResource(id = R.string.dialog_delegate_unknown_error))
    }
}

@Composable
private fun UserProfileScreen(
    screenState: UserProfileViewModel.UserProfileScreenState,
    navController: NavController,
    onConfirmToUnlinkStrava: () -> Unit,
    onClickSaveUserProfile: () -> Unit,
    onFormDataChanged: (UserProfileViewModel.UserProfileFormData) -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize()) {
        StatusBarSpacer()
        val isSaveButtonVisible =
            screenState is UserProfileViewModel.UserProfileScreenState.FormState
        UserProfileTopBar(
            isSaveButtonVisible,
            navController,
            onClickSaveUserProfile
        )
        Box(
            modifier = Modifier
                .weight(1f)
                .background(Color.White)
        ) {
            when (screenState) {
                UserProfileViewModel.UserProfileScreenState.Loading ->
                    CentralLoadingView(
                        text = stringResource(id = R.string.user_profile_fullscreen_loading_text)
                    )
                is UserProfileViewModel.UserProfileScreenState.ErrorRetry ->
                    CentralAnnouncementView(
                        text = stringResource(id = R.string.dialog_delegate_unknown_error) +
                            "\n${screenState.exception.localizedMessage}"
                    ) { }
                is UserProfileViewModel.UserProfileScreenState.FormState -> {
                    UserProfileForm(
                        screenState.editingFormData,
                        screenState.stravaLinkingState,
                        onConfirmToUnlinkStrava,
                        onFormDataChanged
                    )
                }
            }
        }
        NavigationBarSpacer()
    }
}

@Composable
private fun BoxScope.UserProfileLoadingIndicator() {
    LinearProgressIndicator(
        modifier = Modifier
            .align(Alignment.TopCenter)
            .fillMaxWidth()
    )
}

@Composable
private fun UserProfileForm(
    formData: UserProfileViewModel.UserProfileFormData,
    stravaLinkingState: UserProfileViewModel.StravaLinkingState,
    onConfirmToUnlinkStrava: () -> Unit,
    onUserProfileFormDataChanged: (UserProfileViewModel.UserProfileFormData) -> Unit,
) {
    val context = LocalContext.current
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.verticalScroll(rememberScrollState())
    ) {
        Spacer(modifier = Modifier.height(40.dp))
        UserProfileImageView(formData.photoUrl) {
            openUploadAvatarActivity(context)
        }
        UserProfileSectionSpacer()
        UserProfileSectionSpacer()
        SectionTitle(stringResource(id = R.string.profile_basic_label))

        // name
        val userNameErrorMessage = if (formData.isNameValid()) {
            null
        } else {
            stringResource(id = R.string.user_profile_error_empty_user_name)
        }
        UserProfileTextField(
            label = stringResource(id = R.string.user_profile_hint_name),
            value = formData.name,
            onValueChange = { name -> onUserProfileFormDataChanged(formData.copy(name = name)) },
            errorMessage = userNameErrorMessage
        )

        UserProfileSectionSpacer()
        SectionTitle(stringResource(id = R.string.profile_physical_label))

        // birthdate
        UserProfileReadOnlyTextField(
            label = stringResource(id = R.string.user_profile_hint_birthdate),
            value = formatBirthdateMillis(formData.birthdate),
            onClick = {
                showDatePicker(
                    context,
                    formData.birthdate
                ) { selectedBirthdate ->
                    onUserProfileFormDataChanged(formData.copy(birthdate = selectedBirthdate))
                }
            }
        )

        // gender
        UserProfileGenderTextField(formData, onUserProfileFormDataChanged)

        UserProfileTextField(
            label = stringResource(id = R.string.user_profile_hint_weight),
            value = formData.weight,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            onValueChange = { editWeight ->
                val selectedWeight = filterFloatTextField(formData.weight, editWeight)
                onUserProfileFormDataChanged(formData.copy(weight = selectedWeight))
            }
        )

        UserProfileSectionSpacer()
        SectionTitle(stringResource(id = R.string.profile_other_apps_section_title))

        // strava
        UserProfileStravaLinkingSwitch(stravaLinkingState, onConfirmToUnlinkStrava)
    }
}

@Composable
private fun UserProfileGenderTextField(
    formData: UserProfileViewModel.UserProfileFormData,
    onUserProfileFormDataChanged: (UserProfileViewModel.UserProfileFormData) -> Unit,
) {
    var isGenderDialogShowing by remember { mutableStateOf(false) }
    UserProfileReadOnlyTextField(
        label = stringResource(id = R.string.user_profile_hint_gender),
        value = formatGender(formData.gender),
        onClick = { isGenderDialogShowing = true }
    )

    if (isGenderDialogShowing) {
        GenderDialog({ selectedGender ->
            onUserProfileFormDataChanged(formData.copy(gender = selectedGender))
            isGenderDialogShowing = false
        }) { isGenderDialogShowing = false }
    }
}

@Composable
private fun UserProfileStravaLinkingSwitch(
    stravaLinkingState: UserProfileViewModel.StravaLinkingState,
    onConfirmToUnlinkStrava: () -> Unit,
) {
    var isStravaUnlinkAlertShowing by remember { mutableStateOf(false) }
    val context = LocalContext.current
    UserProfileSwitch(
        label = stringResource(id = R.string.user_profile_strava_description),
        enabled = stravaLinkingState != UserProfileViewModel.StravaLinkingState.Unknown,
        checked = stravaLinkingState == UserProfileViewModel.StravaLinkingState.Linked,
        onClick = {
            when (stravaLinkingState) {
                UserProfileViewModel.StravaLinkingState.NotLinked ->
                    openStravaLinkActivity(context)
                UserProfileViewModel.StravaLinkingState.Linked ->
                    isStravaUnlinkAlertShowing = true
                else -> { /* do nothing */
                }
            }
        }
    )

    if (isStravaUnlinkAlertShowing) {
        StravaUnlinkAlert(
            onDismissRequest = { isStravaUnlinkAlertShowing = false },
            onConfirmed = { onConfirmToUnlinkStrava() }
        )
    }
}

fun openUploadAvatarActivity(context: Context) {
    val intent = UploadAvatarActivity.launchIntent(context)
    context.startActivity(intent)
}

@Composable
private fun StravaUnlinkAlert(onDismissRequest: () -> Unit, onConfirmed: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        buttons = {
            Row(
                horizontalArrangement = Arrangement.End,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = AppDimensions.rowVerticalPadding)
                    .padding(horizontal = AppDimensions.screenHorizontalPadding)
            ) {
                TextButton(
                    onClick = {
                        onConfirmed()
                        onDismissRequest()
                    }
                ) {
                    Text(text = stringResource(id = R.string.action_yes), fontSize = 18.sp)
                }
                TextButton(
                    onClick = { onDismissRequest() }
                ) {
                    Text(text = stringResource(id = R.string.action_no), fontSize = 18.sp)
                }
            }
        },
        text = {
            Text(
                text = stringResource(id = R.string.user_profile_app_strava_unlink_dialog_message),
                fontSize = 16.sp
            )
        }
    )
}

fun openStravaLinkActivity(context: Context) {
    val intent = LinkStravaDelegate.buildStravaLoginIntent(context)
    context.startActivity(intent)
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun GenderDialog(onGenderSelect: (Gender) -> Unit, onDismissRequest: () -> Unit) {
    Dialog(onDismissRequest = onDismissRequest) {
        Surface(shape = MaterialTheme.shapes.medium) {
            Column {
                Gender.values().forEach { gender ->
                    ListItem(
                        text = { Text(text = formatGender(gender)) },
                        modifier = Modifier.clickable {
                            onGenderSelect(gender)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun formatGender(gender: Gender): String = when (gender) {
    Gender.Male -> stringResource(id = R.string.user_profile_gender_name_male)
    Gender.Female -> stringResource(id = R.string.user_profile_gender_name_female)
    Gender.Others -> stringResource(id = R.string.user_profile_gender_name_others)
}

@SuppressLint("SimpleDateFormat")
@Composable
fun formatBirthdateMillis(birthdateMillis: Long): String = if (birthdateMillis == 0L) {
    stringResource(id = R.string.user_profile_select_birthdate_instruction)
} else {
    val birthdateFormatter = remember { SimpleDateFormat("MM/dd/yyyy") }
    birthdateFormatter.format(Date(birthdateMillis))
}

fun showDatePicker(context: Context, userBirthdateInMillis: Long, onDateSelect: (Long) -> Unit) {
    val birthdateCalendar = Calendar.getInstance()
    if (userBirthdateInMillis > 0) {
        birthdateCalendar.timeInMillis = userBirthdateInMillis
    }
    val birthYear = birthdateCalendar[Calendar.YEAR]
    val birthMonth = birthdateCalendar[Calendar.MONTH]
    val birthDayOfMonth = birthdateCalendar[Calendar.DAY_OF_MONTH]
    DatePickerDialog(
        context,
        { _, selectedYear, selectedMonth, selectedDayOfMonth ->
            val selectedCalendar = Calendar.getInstance()
            selectedCalendar[Calendar.YEAR] = selectedYear
            selectedCalendar[Calendar.MONTH] = selectedMonth
            selectedCalendar[Calendar.DAY_OF_MONTH] = selectedDayOfMonth
            onDateSelect(selectedCalendar.timeInMillis)
        },
        birthYear,
        birthMonth,
        birthDayOfMonth
    )
        .show()
}

private fun Modifier.modifyIf(enabled: Boolean, application: Modifier.() -> Modifier): Modifier =
    this.run {
        if (enabled) {
            this.application()
        } else {
            this
        }
    }

@Composable
private fun UserProfileSwitch(
    label: String,
    checked: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
) = Row(
    verticalAlignment = Alignment.CenterVertically,
    modifier = Modifier
        .modifyIf(enabled) {
            clickable { onClick() }
        }
        .padding(
            vertical = AppDimensions.rowVerticalPadding,
            horizontal = AppDimensions.screenHorizontalPadding
        )
) {
    Text(text = label, modifier = Modifier.weight(1f))
    Switch(enabled = enabled, checked = checked, onCheckedChange = null)
}

@Preview
@Composable
private fun PreviewUserProfileSwitch() {
    UserProfileSwitch("label", checked = true, enabled = true) {}
}

@Composable
private fun UserProfileTextField(
    label: String,
    value: String,
    keyboardOptions: KeyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
    readOnly: Boolean = false,
    onValueChange: (String) -> Unit,
    errorMessage: String? = null,
) {
    Column(modifier = Modifier.padding(horizontal = AppDimensions.screenHorizontalPadding)) {
        OutlinedTextField(
            keyboardOptions = keyboardOptions,
            maxLines = 1,
            singleLine = true,
            isError = errorMessage != null,
            readOnly = readOnly,
            modifier = Modifier.fillMaxWidth(),
            label = { Text(text = label) },
            value = value,
            onValueChange = onValueChange
        )
        Text(
            text = errorMessage ?: "", // let the space still be occupied when no error message
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 0.dp, top = 4.dp),
            color = AppColors.error(),
            fontSize = 12.sp
        )

        Spacer(modifier = Modifier.height(4.dp))
    }
}

@Composable
private fun UserProfileReadOnlyTextField(
    label: String,
    value: String,
    onClick: () -> Unit,
) {
    Box {
        UserProfileTextField(label = label, value = value, readOnly = true, onValueChange = { })
        Box(
            modifier = Modifier
                .matchParentSize()
                .clickable { onClick() }
        )
    }
}

@Composable
private fun SectionTitle(titleText: String) =
    Column(modifier = Modifier.padding(horizontal = AppDimensions.screenHorizontalPadding)) {
        Text(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp),
            text = titleText.uppercase(),
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp
        )
    }

@Composable
private fun UserProfileSectionSpacer() {
    Spacer(modifier = Modifier.height(16.dp))
}

@Composable
private fun UserProfileTopBar(
    isSaveButtonVisible: Boolean,
    navController: NavController,
    onClickSave: () -> Unit,
) {
    TopAppBar(
        navigationIcon = {
            AppBarIconButton(iconImageVector = Icons.Sharp.ArrowBack) {
                navController.popBackStack()
            }
        },
        title = { Text(text = stringResource(id = R.string.user_profile_title)) },
        actions = {
            if (isSaveButtonVisible) {
                AppBarTextButton(
                    text = stringResource(id = R.string.action_save),
                    onClick = { onClickSave() }
                )
            }
        }
    )
}

@OptIn(ExperimentalCoilApi::class)
@Composable
private fun UserProfileImageView(
    photoUrl: String?,
    imageLoadSizeDp: Dp = 100.dp,
    onClick: () -> Unit,
) {
    val imageLoadSizePx = with(LocalDensity.current) { imageLoadSizeDp.roundToPx() }
    Surface(shape = CircleShape, modifier = Modifier.size(imageLoadSizeDp)) {
        Box {
            Image(
                painter = rememberImagePainter(
                    data = photoUrl,
                    builder = {
                        size(imageLoadSizePx)
                        placeholder(R.drawable.common_avatar_placeholder_image)
                        error(R.drawable.common_avatar_placeholder_image)
                        scale(Scale.FILL)
                    }
                ),
                contentDescription = "Athlete avatar",
                modifier = Modifier
                    .matchParentSize()
                    .clickable { onClick() }
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .background(Color(0x55000000))
            ) {
                Icon(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .scale(0.75f),
                    imageVector = Icons.Sharp.PhotoCamera,
                    tint = Color.White,
                    contentDescription = "photo camera icon"
                )
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xffffffff)
@Composable
private fun PreviewUserProfileScreenSuccessForm() {
    UserProfileScreen(
        screenState = UserProfileViewModel.UserProfileScreenState.create(
            Resource.Success(createUserProfile()), null, Resource.Success(createExternalProviders())
        ),
        navController = rememberNavController(),
        {},
        {},
        {}
    )
}

@Preview(showBackground = true, backgroundColor = 0xffffffff)
@Composable
private fun PreviewUserProfileScreenErrorForm() {
    UserProfileScreen(
        screenState = UserProfileViewModel.UserProfileScreenState.create(
            Resource.Error(Exception()), null, Resource.Success(createExternalProviders())
        ),
        navController = rememberNavController(),
        {},
        {},
        {}
    )
}

private fun createExternalProviders() =
    ExternalProviders(
        ProviderToken(
            RunningApp.Strava,
            ExternalAppToken.StravaToken(
                "accessToken", "refreshToken", StravaAthlete(0L)
            )
        )
    )

private fun createUserProfile(): UserProfile {
    return UserProfile(
        "defaultUserId",
        "name",
        Gender.Male,
        65f,
        null
    )
}
