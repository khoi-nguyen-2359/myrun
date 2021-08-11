package akio.apps.myrun.feature.configurator.ui

import akio.apps.myrun.feature.configurator.RouteTrackingConfigurationViewModel
import akio.apps.myrun.feature.configurator.ui.SectionSpacing.elementHorizontalPadding
import akio.apps.myrun.feature.configurator.ui.SectionSpacing.elementVerticalPadding
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Button
import androidx.compose.material.Checkbox
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ExpandLess
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun ConfiguratorScreen(routeTrackingViewModel: RouteTrackingConfigurationViewModel) =
    ConfiguratorTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            RouteTrackingSection(routeTrackingViewModel)
        }
    }

object SectionSpacing {
    val elementVerticalPadding = 8.dp
    val elementHorizontalPadding = 8.dp
}

@Composable
private fun RouteTrackingSection(routeTrackingViewModel: RouteTrackingConfigurationViewModel) =
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
                text = "Route Tracking",
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .padding(vertical = elementVerticalPadding)
                    .fillMaxWidth()
            )
        }
        if (isExpanded) {
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
    }

@Composable
private fun LocationUpdateConfiguration(
    config: RouteTrackingConfigurationViewModel.LocationUpdateConfiguration,
    onValueChanged: (RouteTrackingConfigurationViewModel.LocationUpdateConfiguration) -> Unit
) {
    Text(text = "Location Update:", modifier = Modifier.padding(vertical = elementVerticalPadding))
    TextField(
        label = { Text("Update interval") },
        value = config.updateInterval,
        onValueChange = { value -> onValueChanged(config.copy(updateInterval = value)) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        maxLines = 1
    )
    TextField(
        label = { Text("Fastest update interval") },
        value = config.fastestUpdateInterval,
        onValueChange = { value -> onValueChanged(config.copy(fastestUpdateInterval = value)) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        maxLines = 1
    )
    TextField(
        label = { Text("Smallest displacement") },
        value = config.smallestDisplacement,
        onValueChange = { value -> onValueChanged(config.copy(smallestDisplacement = value)) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        maxLines = 1
    )
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = elementVerticalPadding)
    ) {
        Checkbox(
            checked = config.isAvgAccumulationEnabled,
            onCheckedChange = { value ->
                onValueChanged(config.copy(isAvgAccumulationEnabled = value))
            }
        )
        Spacer(modifier = Modifier.size(elementHorizontalPadding))
        Text(text = "Use average location accumulator")
    }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = elementVerticalPadding)
    ) {
        Checkbox(
            checked = config.isSpeedFilterEnabled,
            onCheckedChange = { value ->
                onValueChanged(config.copy(isSpeedFilterEnabled = value))
            }
        )
        Spacer(modifier = Modifier.size(elementHorizontalPadding))
        Text(text = "Use location speed filter")
    }
}

@Composable
private fun ApplyButton(onClick: () -> Unit) = Button(
    onClick = onClick,
    modifier = Modifier.padding(vertical = elementVerticalPadding)
) {
    Text(text = "Apply")
}

@Preview
@Composable
private fun PreviewLocationRequestConfiguration() = Column(modifier = Modifier.fillMaxWidth()) {
    LocationUpdateConfiguration(
        RouteTrackingConfigurationViewModel.LocationUpdateConfiguration(),
        onValueChanged = { }
    )
}
