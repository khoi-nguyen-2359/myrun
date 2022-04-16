package akio.apps.myrun.feature.configurator.ui

import akio.apps.myrun.feature.base.ui.AppTheme
import akio.apps.myrun.feature.configurator.viewmodel.RouteTrackingSectionViewModel
import akio.apps.myrun.feature.configurator.viewmodel.UserAuthenticationSectionViewModel
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
    userAuthenticationSectionSectionViewModel: UserAuthenticationSectionViewModel,
) =
    AppTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            RouteTrackingSection(routeTrackingViewModel)
            UserAuthenticationSection(userAuthenticationSectionSectionViewModel)
        }
    }

object SectionSpacing {
    val elementVerticalPadding = 8.dp
    val elementHorizontalPadding = 8.dp
}

@Composable
private fun RouteTrackingSection(routeTrackingViewModel: RouteTrackingSectionViewModel) =
    ExpandableSection(label = "Route Tracking") {
        val locationUpdateConfig by routeTrackingViewModel.locationUpdateConfigFlow
            .collectAsState(RouteTrackingSectionViewModel.LocationUpdateConfiguration())
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
