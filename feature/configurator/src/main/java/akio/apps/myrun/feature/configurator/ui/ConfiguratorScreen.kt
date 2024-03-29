package akio.apps.myrun.feature.configurator.ui

import akio.apps.myrun.feature.configurator.viewmodel.LocationPresentViewModel
import akio.apps.myrun.feature.configurator.viewmodel.RouteTrackingSectionViewModel
import akio.apps.myrun.feature.configurator.viewmodel.UserAuthenticationSectionViewModel
import akio.apps.myrun.feature.core.ui.AppTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ConfiguratorScreen(
    routeTrackingViewModel: RouteTrackingSectionViewModel,
    locationPresentationViewModel: LocationPresentViewModel,
    userAuthenticationSectionSectionViewModel: UserAuthenticationSectionViewModel,
) =
    AppTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            RouteTrackingSection(routeTrackingViewModel, locationPresentationViewModel)
            UserAuthenticationSection(userAuthenticationSectionSectionViewModel)
        }
    }

object SectionSpacing {
    val elementVerticalPadding = 8.dp
    val elementHorizontalPadding = 8.dp
}

@Composable
private fun RouteTrackingSection(
    routeTrackingViewModel: RouteTrackingSectionViewModel,
    locationPresentationViewModel: LocationPresentViewModel,
) = ExpandableSection(label = "Route Tracking") {
    val locationUpdateConfig by routeTrackingViewModel.locationUpdateConfigFlow
        .collectAsState(RouteTrackingSectionViewModel.LocationUpdateConfiguration())
    LocationUpdateConfiguration(
        locationUpdateConfig,
        onValueChanged = { value ->
            routeTrackingViewModel.onLocationUpdateConfigurationChanged(value)
        }
    )
    val isBSplinesEnabled by
        locationPresentationViewModel.isBSplinesEnabledFlow.collectAsState(initial = false)
    LocationPresentConfiguration(isBSplinesEnabled, locationPresentationViewModel::updateConfig)
    ApplyButton { routeTrackingViewModel.applyChanges() }
}

@Composable
private fun ApplyButton(onClick: () -> Unit) = Button(
    onClick = onClick,
    modifier = Modifier.padding(vertical = SectionSpacing.elementVerticalPadding)
) {
    Text(text = "Apply")
}
