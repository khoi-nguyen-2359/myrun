package akio.apps.myrun.feature.configurator.ui

import akio.apps.myrun.feature.base.ui.AppTheme
import akio.apps.myrun.feature.configurator.RouteTrackingConfigurationViewModel
import akio.apps.myrun.feature.configurator.ui.SectionSpacing.elementVerticalPadding
import akio.apps.myrun.feature.configurator.viewmodel.UserAuthenticationViewModel
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ExpandLess
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun ConfiguratorScreen(
    routeTrackingViewModel: RouteTrackingConfigurationViewModel,
    userAuthenticationSectionViewModel: UserAuthenticationViewModel,
) =
    AppTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            RouteTrackingSection(routeTrackingViewModel)
            UserAuthenticationSection(userAuthenticationSectionViewModel)
        }
    }

@Composable
fun UserAuthenticationSection(userAuthenticationSectionViewModel: UserAuthenticationViewModel) {
    ExpandableSection(label = "User") {
        val userProfileState = userAuthenticationSectionViewModel.userProfileFlow.collectAsState(
            initial = null
        )
        val userProfile = userProfileState.value ?: return@ExpandableSection
        Text(text = "User ID: ${userProfile.accountId}")
    }
}

object SectionSpacing {
    val elementVerticalPadding = 8.dp
    val elementHorizontalPadding = 8.dp
}

@Composable
private fun ExpandableSection(label: String, content: @Composable () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize()
    ) {
        var isExpanded by remember { mutableStateOf(true) }
        Row(
            modifier = Modifier.clickable {
                isExpanded = !isExpanded
            }
        ) {
            Icon(
                imageVector = if (isExpanded) {
                    Icons.Rounded.ExpandLess
                } else {
                    Icons.Rounded.ExpandMore
                },
                contentDescription = "expand icon",
                modifier = Modifier.padding(vertical = elementVerticalPadding)
            )
            Text(
                text = label,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .padding(vertical = elementVerticalPadding)
                    .fillMaxWidth()
            )
        }
        if (isExpanded) {
            content()
        }
    }
}

@Composable
private fun RouteTrackingSection(routeTrackingViewModel: RouteTrackingConfigurationViewModel) =
    ExpandableSection(label = "Route Tracking") {
        val locationUpdateConfig by routeTrackingViewModel.locationUpdateConfigFlow
            .collectAsState(RouteTrackingConfigurationViewModel.LocationUpdateConfiguration())
        LocationUpdateConfiguration(
            locationUpdateConfig,
            onValueChanged = { value ->
                routeTrackingViewModel.onLocationUpdateConfigurationChanged(value)
            }
        )
        ApplyButton { routeTrackingViewModel.applyChanges() }
    }

@Composable
private fun ApplyButton(onClick: () -> Unit) = Button(
    onClick = onClick,
    modifier = Modifier.padding(vertical = SectionSpacing.elementVerticalPadding)
) {
    Text(text = "Apply")
}
