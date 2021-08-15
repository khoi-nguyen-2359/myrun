package akio.apps.myrun.feature.profile.ui

import akio.apps.common.data.Resource
import akio.apps.myrun.data.user.api.model.Gender
import akio.apps.myrun.data.user.api.model.UserProfile
import akio.apps.myrun.feature.base.ui.AppBarIconButton
import akio.apps.myrun.feature.base.ui.AppColors
import akio.apps.myrun.feature.base.ui.AppDimensions
import akio.apps.myrun.feature.base.ui.AppTheme
import akio.apps.myrun.feature.profile.R
import akio.apps.myrun.feature.profile.UserProfileViewModel
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Switch
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.sharp.ArrowBack
import androidx.compose.material.icons.sharp.Save
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.capitalize
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.rememberImagePainter
import com.google.accompanist.insets.statusBarsHeight
import timber.log.Timber

@Composable
fun UserProfileScreen(
    navController: NavController,
    userProfileViewModel: UserProfileViewModel,
) = AppTheme {
    val userProfileResourceState =
        userProfileViewModel.userProfileResourceFlow.collectAsState(initial = null)
    Timber.d("userProfile=${userProfileResourceState.value}")
    val userProfileResource = userProfileResourceState.value ?: return@AppTheme
    UserProfileScreen(navController, userProfileResource)
}

@Composable
private fun UserProfileScreen(
    navController: NavController,
    userProfileResource: Resource<UserProfile>
) {
    Column {
        Spacer(
            modifier = Modifier
                .statusBarsHeight()
                .fillMaxWidth()
                .background(AppColors.primarySurface())
        )
        UserProfileTopBar(navController)
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .padding(horizontal = AppDimensions.screenHorizontalPadding)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(40.dp))
            UserProfileImageView(userProfileResource.data?.photo)
            UserProfileSectionSpacer()
            SectionTitle(stringResource(id = R.string.profile_basic_label))
            UserProfileTextField(
                placeholder = stringResource(id = R.string.user_profile_hint_name),
                value = userProfileResource.data?.name ?: "",
                onValueChange = { }
            )
            SectionTitle(stringResource(id = R.string.profile_physical_label))
            UserProfileTextField(
                placeholder = stringResource(id = R.string.user_profile_hint_birthdate),
                value = userProfileResource.data?.name ?: "",
                onValueChange = { }
            )
            UserProfileTextField(
                placeholder = stringResource(id = R.string.user_profile_hint_gender),
                value = userProfileResource.data?.gender?.toString() ?: "",
                onValueChange = { }
            )
            UserProfileTextField(
                placeholder = stringResource(id = R.string.user_profile_hint_weight),
                value = userProfileResource.data?.weight?.toString() ?: "",
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
    placeholder: String,
    value: String,
    onValueChange: (String) -> Unit,
) {
    OutlinedTextField(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp),
        placeholder = { Text(text = placeholder) },
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
        text = titleText.capitalize(Locale.current),
        fontWeight = FontWeight.Bold,
        fontSize = 14.sp
    )
}

@Composable
private fun UserProfileSectionSpacer() {
    Spacer(modifier = Modifier.height(20.dp))
}

@Composable
private fun UserProfileTopBar(navController: NavController) {
    TopAppBar(
        navigationIcon = {
            AppBarIconButton(iconImageVector = Icons.Sharp.ArrowBack) {
                navController.popBackStack()
            }
        },
        title = { Text(text = stringResource(id = R.string.user_profile_title)) },
        actions = {
            AppBarIconButton(iconImageVector = Icons.Sharp.Save) {
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

@Preview
@Composable
private fun PreviewUserProfileScreen() {
    UserProfileScreen(
        navController = rememberNavController(),
        userProfileResource = Resource.Success(
            createUserProfile()
        )
    )
}

private fun createUserProfile(): UserProfile {
    return UserProfile(
        "defaultUserId",
        "name",
        "defaultEmail",
        "defaultPhoneNumber",
        Gender.male,
        1.70f,
        65f,
        null
    )
}
