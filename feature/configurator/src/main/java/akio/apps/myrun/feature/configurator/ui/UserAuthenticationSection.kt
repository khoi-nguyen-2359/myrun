package akio.apps.myrun.feature.configurator.ui

import akio.apps.myrun.feature.configurator.viewmodel.UserAuthenticationSectionViewModel
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState

@Composable
fun UserAuthenticationSection(
    userAuthenticationSectionSectionViewModel: UserAuthenticationSectionViewModel
) {
    ExpandableSection(label = "User") {
        val userProfileState =
            userAuthenticationSectionSectionViewModel.userProfileFlow.collectAsState(
                initial = null
            )
        val userProfile = userProfileState.value ?: return@ExpandableSection
        Text(text = "User ID: ${userProfile.accountId}")
    }
}
