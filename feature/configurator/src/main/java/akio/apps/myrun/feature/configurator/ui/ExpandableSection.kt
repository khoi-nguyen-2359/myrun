package akio.apps.myrun.feature.configurator.ui

import akio.apps.myrun.feature.configurator.ui.SectionSpacing.elementVerticalPadding
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Icon
import androidx.compose.material.Text
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

@Composable
fun ExpandableSection(label: String, content: @Composable () -> Unit) {
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
