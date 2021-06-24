package akio.apps.myrun.configurator.ui

import akio.apps.myrun.configurator.RouteTrackingConfigurationViewModel
import akio.apps.myrun.configurator.ui.SectionSpacing.elementVerticalPadding
import akio.apps.myrun.data.routetracking.model.LocationRequestConfig
import android.util.Log
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Button
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ExpandLess
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
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
}

@Composable
private fun RouteTrackingSection(routeTrackingViewModel: RouteTrackingConfigurationViewModel) =
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize()
    ) {
        var isExpanded by remember { mutableStateOf(true) }
        Row(modifier = Modifier.clickable { isExpanded = !isExpanded }) {
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
            val requestInfo by produceState<LocationRequestConfig?>(
                initialValue = null,
                producer = {
                    value = routeTrackingViewModel.getLocationRequestConfig()
                }
            )
            requestInfo?.let {
                LocationRequestConfiguration(
                    requestConfig = it,
                    onValueChanged = {
                        routeTrackingViewModel.setLocationRequestInfo(it)
                    }
                )
            }
        }
    }

@Composable
private fun LocationRequestConfiguration(
    requestConfig: LocationRequestConfig,
    onValueChanged: (LocationRequestConfig) -> Unit
) {
    var updateInterval by remember { mutableStateOf(requestConfig.updateInterval.toString()) }
    var fastestUpdateInterval by remember {
        mutableStateOf(requestConfig.fastestUpdateInterval.toString())
    }
    var smallestDisplacement by remember {
        mutableStateOf(requestConfig.smallestDisplacement.toString())
    }
    Log.d("khoi", "location config = $updateInterval $fastestUpdateInterval $smallestDisplacement")
    Text(text = "Location Request:", modifier = Modifier.padding(vertical = elementVerticalPadding))
    TextField(
        label = { Text("Update interval") },
        value = updateInterval,
        onValueChange = { value -> updateInterval = value },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        maxLines = 1
    )
    TextField(
        label = { Text("Fastest update interval") },
        value = fastestUpdateInterval,
        onValueChange = { value -> fastestUpdateInterval = value },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        maxLines = 1
    )
    TextField(
        label = { Text("Smallest displacement") },
        value = smallestDisplacement,
        onValueChange = { value -> smallestDisplacement = value },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        maxLines = 1
    )

    ApplyButton {
        val updateValue = LocationRequestConfig(
            updateInterval.toLongOrNull() ?: requestConfig.updateInterval,
            fastestUpdateInterval.toLongOrNull() ?: requestConfig.fastestUpdateInterval,
            smallestDisplacement.toFloatOrNull() ?: requestConfig.smallestDisplacement
        )
        onValueChanged(updateValue)
        updateInterval = updateValue.updateInterval.toString()
        fastestUpdateInterval = updateValue.fastestUpdateInterval.toString()
        smallestDisplacement = updateValue.smallestDisplacement.toString()
    }
}

@Composable
private fun ApplyButton(onClick: () -> Unit) = Button(
    onClick = onClick,
    modifier = Modifier.padding(vertical = elementVerticalPadding)
) {
    Text(text = "Apply")
}
