package akio.apps.myrun.feature.profile.ui

import akio.apps.common.data.Resource
import akio.apps.myrun.data.user.api.model.Gender
import akio.apps.myrun.data.user.api.model.UserProfile
import akio.apps.myrun.feature.base.ui.AppBarIconButton
import akio.apps.myrun.feature.base.ui.AppColors
import akio.apps.myrun.feature.base.ui.AppDimensions
import akio.apps.myrun.feature.base.ui.AppTheme
import akio.apps.myrun.feature.base.ui.FullscreenAnnouncementView
import akio.apps.myrun.feature.base.ui.FullscreenLoadingView
import akio.apps.myrun.feature.base.ui.NavigationBarSpacer
import akio.apps.myrun.feature.base.ui.StatusBarSpacer
import akio.apps.myrun.feature.profile.R
import akio.apps.myrun.feature.profile.UserProfileViewModel
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Snackbar
import androidx.compose.material.Switch
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.contentColorFor
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.sharp.ArrowBack
import androidx.compose.material.icons.sharp.Save
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.rememberImagePainter

private sealed class UserProfileScreenState {
    object FullScreenLoading : UserProfileScreenState()
    object FullScreenError : UserProfileScreenState()

    sealed class UserProfileForm(val formData: UserProfileFormData) : UserProfileScreenState()
    class UserProfileLoadingForm(formData: UserProfileFormData) : UserProfileForm(formData)
    class UserProfileErrorForm(formData: UserProfileFormData) : UserProfileForm(formData)
    class UserProfileSuccessForm(formData: UserProfileFormData) : UserProfileForm(formData)

    object UnknownState : UserProfileScreenState()

    companion object {
        fun createFromUserProfileResource(resource: Resource<UserProfile>): UserProfileScreenState {
            val data = resource.data
            return when {
                resource is Resource.Loading && data == null -> FullScreenLoading
                resource is Resource.Error && data == null -> FullScreenError
                resource is Resource.Loading && data != null -> UserProfileLoadingForm(
                    UserProfileFormData.createFromUserProfile(data)
                )
                resource is Resource.Error && data != null -> UserProfileErrorForm(
                    UserProfileFormData.createFromUserProfile(data)
                )
                resource is Resource.Success && data != null -> UserProfileSuccessForm(
                    UserProfileFormData.createFromUserProfile(data)
                )
                else -> UnknownState
            }
        }
    }
}

private data class UserProfileFormData private constructor(
    val name: String,
    val photoUrl: String?,
    val birthdate: Long,
    val gender: Gender,
    val weight: Float,
) {
    companion object {
        fun createFromUserProfile(userProfile: UserProfile) = UserProfileFormData(
            name = userProfile.name,
            photoUrl = userProfile.photo,
            birthdate = userProfile.birthdate,
            gender = userProfile.gender,
            weight = userProfile.weight
        )
    }
}

@Composable
fun UserProfileScreen(
    navController: NavController,
    userProfileViewModel: UserProfileViewModel,
) = AppTheme {
    val userProfileResource by
    userProfileViewModel.userProfileResourceFlow.collectAsState(initial = Resource.Loading())
    UserProfileScreen(navController, userProfileResource)
}

@Composable
private fun UserProfileScreen(
    navController: NavController,
    userProfileResource: Resource<UserProfile>,
) {
    val screenState = UserProfileScreenState.createFromUserProfileResource(userProfileResource)
    Column(modifier = Modifier.fillMaxSize()) {
        StatusBarSpacer()
        UserProfileTopBar(
            navController,
            screenState is UserProfileScreenState.UserProfileSuccessForm
        )
        Box(modifier = Modifier.weight(1f)) {
            when (screenState) {
                UserProfileScreenState.FullScreenLoading ->
                    FullscreenLoadingView(
                        text = stringResource(id = R.string.user_profile_fullscreen_loading_text)
                    )
                UserProfileScreenState.FullScreenError ->
                    FullscreenAnnouncementView(
                        text = stringResource(id = R.string.user_profile_fullscreen_loading_error)
                    ) {
                        // todo: retry
                    }
                is UserProfileScreenState.UserProfileForm -> {
                    if (screenState is UserProfileScreenState.UserProfileLoadingForm) {
                        UserProfileLoadingIndicator()
                    }

                    UserProfileScrollableForm(screenState)

                    if (screenState is UserProfileScreenState.UserProfileErrorForm) {
                        UserProfileErrorSnackbar(modifier = Modifier.align(Alignment.BottomCenter))
                    }
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
fun UserProfileErrorSnackbar(modifier: Modifier = Modifier) {
    val snackbarBgColor = AppColors.error()
    val snackbarContentColor = contentColorFor(backgroundColor = snackbarBgColor)
    Snackbar(
        backgroundColor = snackbarBgColor,
        contentColor = snackbarContentColor,
        action = {
            Button(onClick = { /*TODO*/ }) {
                Text(text = "Retry")
            }
        },
        modifier = modifier.padding(8.dp)
    ) {
        Text(text = "Please try to fetch your profile again.")
    }
}

@Composable
private fun UserProfileScrollableForm(screenState: UserProfileScreenState.UserProfileForm) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .padding(horizontal = AppDimensions.screenHorizontalPadding)
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(modifier = Modifier.height(40.dp))
        UserProfileImageView(screenState.formData.photoUrl)
        UserProfileImageView(screenState.formData.photoUrl)
        UserProfileImageView(screenState.formData.photoUrl)
        UserProfileImageView(screenState.formData.photoUrl)
        UserProfileSectionSpacer()
        SectionTitle(stringResource(id = R.string.profile_basic_label))
        UserProfileTextField(
            label = stringResource(id = R.string.user_profile_hint_name),
            value = screenState.formData.name,
            onValueChange = { }
        )
        SectionTitle(stringResource(id = R.string.profile_physical_label))
        UserProfileTextField(
            label = stringResource(id = R.string.user_profile_hint_birthdate),
            value = screenState.formData.name,
            onValueChange = { }
        )
        UserProfileTextField(
            label = stringResource(id = R.string.user_profile_hint_gender),
            value = screenState.formData.gender.toString(),
            onValueChange = { }
        )
        UserProfileTextField(
            label = stringResource(id = R.string.user_profile_hint_weight),
            value = screenState.formData.weight.toString(),
            onValueChange = { }
        )
        SectionTitle(stringResource(id = R.string.profile_other_apps_section_title))
        UserProfileSwitch(
            label = stringResource(id = R.string.user_profile_strava_description),
            checked = true,
            onCheckedChange = { }
        )
    }
}

@Composable
private fun UserProfileSwitch(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) = Row {
    Text(text = label, modifier = Modifier.weight(1f))
    Switch(
        checked = checked,
        onCheckedChange = onCheckedChange
    )
}

@Composable
private fun UserProfileTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
) {
    OutlinedTextField(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp),
        label = { Text(text = label) },
        value = value,
        onValueChange = onValueChange
    )
}

@Composable
private fun SectionTitle(titleText: String) = Column {
    UserProfileSectionSpacer()
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
    Spacer(modifier = Modifier.height(20.dp))
}

@Composable
private fun UserProfileTopBar(
    navController: NavController,
    shouldShowSaveButton: Boolean,
) {
    TopAppBar(
        navigationIcon = {
            AppBarIconButton(iconImageVector = Icons.Sharp.ArrowBack) {
                navController.popBackStack()
            }
        },
        title = { Text(text = stringResource(id = R.string.user_profile_title)) },
        actions = {
            if (shouldShowSaveButton) {
                AppBarIconButton(iconImageVector = Icons.Sharp.Save) { }
            }
        }
    )
}

@Composable
private fun UserProfileImageView(photoUrl: String?, imageLoadSizeDp: Dp = 100.dp) {
    val imageLoadSizePx = with(LocalDensity.current) { imageLoadSizeDp.roundToPx() }
    Image(
        painter = rememberImagePainter(
            data = photoUrl,
            builder = {
                size(imageLoadSizePx)
                placeholder(R.drawable.common_avatar_placeholder_image)
                error(R.drawable.common_avatar_placeholder_image)
            }
        ),
        contentDescription = "Athlete avatar",
        modifier = Modifier
            .size(imageLoadSizeDp)
            .clip(CircleShape)
    )
}

@Preview(showBackground = true, backgroundColor = 0xffffffff)
@Composable
private fun PreviewUserProfileScreenSuccessForm() {
    UserProfileScreen(
        navController = rememberNavController(),
        userProfileResource = Resource.Success(createUserProfile())
    )
}

@Preview(showBackground = true, backgroundColor = 0xffffffff)
@Composable
private fun PreviewUserProfileScreenLoadingFormWithoutData() {
    UserProfileScreen(
        navController = rememberNavController(),
        userProfileResource = Resource.Loading(null)
    )
}

@Preview(showBackground = true, backgroundColor = 0xffffffff)
@Composable
private fun PreviewUserProfileScreenLoadingFormWithData() {
    UserProfileScreen(
        navController = rememberNavController(),
        userProfileResource = Resource.Loading(createUserProfile())
    )
}

@Preview(showBackground = true, backgroundColor = 0xffffffff)
@Composable
private fun PreviewUserProfileScreenErrorFormWithoutData() {
    UserProfileScreen(
        navController = rememberNavController(),
        userProfileResource = Resource.Error(Exception(), data = null)
    )
}

@Preview(showBackground = true, backgroundColor = 0xffffffff)
@Composable
private fun PreviewUserProfileScreenErrorFormWithData() {
    UserProfileScreen(
        navController = rememberNavController(),
        userProfileResource = Resource.Error(Exception(), data = createUserProfile())
    )
}

private fun createUserProfile(): UserProfile {
    return UserProfile(
        "defaultUserId",
        "name",
        "defaultEmail",
        "defaultPhoneNumber",
        Gender.Male,
        65f,
        null
    )
}
