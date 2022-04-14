package akio.apps.myrun.feature.configurator.ui

import akio.apps.myrun.feature.configurator.viewmodel.RouteTrackingSectionViewModel
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Checkbox
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun LocationUpdateConfiguration(
    config: RouteTrackingSectionViewModel.LocationUpdateConfiguration,
    onValueChanged: (RouteTrackingSectionViewModel.LocationUpdateConfiguration) -> Unit,
) {
    Text(
        text = "Location Update:",
        modifier = Modifier.padding(vertical = SectionSpacing.elementVerticalPadding)
    )
    TextField(
        label = { Text("Update interval") },
        value = config.updateInterval,
        onValueChange = { value -> onValueChanged(config.copy(updateInterval = value)) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        maxLines = 1,
        singleLine = true
    )
    TextField(
        label = { Text("Fastest update interval") },
        value = config.fastestUpdateInterval,
        onValueChange = { value -> onValueChanged(config.copy(fastestUpdateInterval = value)) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        maxLines = 1,
        singleLine = true
    )
    TextField(
        label = { Text("Smallest displacement") },
        value = config.smallestDisplacement,
        onValueChange = { value -> onValueChanged(config.copy(smallestDisplacement = value)) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        maxLines = 1,
        singleLine = true
    )
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = SectionSpacing.elementVerticalPadding)
    ) {
        Checkbox(
            checked = config.isAvgAccumulationEnabled,
            onCheckedChange = { value ->
                onValueChanged(config.copy(isAvgAccumulationEnabled = value))
            }
        )
        Spacer(modifier = Modifier.size(SectionSpacing.elementHorizontalPadding))
        Text(text = "Use average location accumulator")
    }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = SectionSpacing.elementVerticalPadding)
    ) {
        Checkbox(
            checked = config.isSpeedFilterEnabled,
            onCheckedChange = { value ->
                onValueChanged(config.copy(isSpeedFilterEnabled = value))
            }
        )
        Spacer(modifier = Modifier.size(SectionSpacing.elementHorizontalPadding))
        Text(text = "Use location speed filter")
    }
}

@Preview(showSystemUi = true, showBackground = true, backgroundColor = 0xffffff)
@Composable
private fun PreviewLocationRequestConfiguration() = Column(modifier = Modifier.fillMaxWidth()) {
    LocationUpdateConfiguration(
        RouteTrackingSectionViewModel.LocationUpdateConfiguration(),
        onValueChanged = { }
    )
}
