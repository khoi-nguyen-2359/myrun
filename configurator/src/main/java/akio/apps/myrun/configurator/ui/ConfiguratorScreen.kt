package akio.apps.myrun.configurator.ui

import akio.apps.myrun.configurator.ui.SectionSpacing.elementVerticalPadding
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ExpandLess
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun ConfiguratorScreen() = ConfiguratorTheme {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        RouteTrackingSection()
    }
}

object SectionSpacing {
    val elementVerticalPadding = 8.dp
}

@Composable
private fun RouteTrackingSection() = Column(
    modifier = Modifier
        .fillMaxWidth()
        .animateContentSize()
) {
    var isExpanded by remember { mutableStateOf(false) }
    Row(modifier = Modifier.clickable { isExpanded = !isExpanded }) {
        Icon(
            imageVector = if (isExpanded) Icons.Rounded.ExpandLess else Icons.Rounded.ExpandMore,
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
        LocationRequestConfiguration()
    }
}

@Composable
private fun LocationRequestConfiguration() {
    var updateInterval by remember { mutableStateOf("2000") }
    var fastestUpdateInterval by remember { mutableStateOf("1000") }
    var smallestDisplacement by remember { mutableStateOf("5") }
    Text(text = "Location Request:")
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
}

@Preview(showSystemUi = true)
@Composable
private fun Preview() = ConfiguratorScreen()
