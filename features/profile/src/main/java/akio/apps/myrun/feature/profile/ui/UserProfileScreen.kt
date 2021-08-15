package akio.apps.myrun.feature.profile.ui

import akio.apps.myrun.feature.base.ui.AppBarIconButton
import akio.apps.myrun.feature.base.ui.AppTheme
import akio.apps.myrun.feature.profile.R
import akio.apps.myrun.feature.profile.UserProfileViewModel
import androidx.compose.foundation.layout.Column
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.sharp.ArrowBack
import androidx.compose.material.icons.sharp.Save
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController

@Suppress("UNUSED_PARAMETER")
@Composable
fun UserProfileScreen(
    navController: NavController,
    userProfileViewModel: UserProfileViewModel,
) = AppTheme {
    Column {
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
}
