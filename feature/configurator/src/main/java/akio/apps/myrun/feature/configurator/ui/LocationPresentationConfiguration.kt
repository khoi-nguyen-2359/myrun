package akio.apps.myrun.feature.configurator.ui

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Checkbox
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight

@Composable
fun LocationPresentConfiguration(
    isBSplinesEnabled: Boolean,
    onValueChanged: (Boolean) -> Unit,
) {
    Text(
        text = "Location Presentation",
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(vertical = SectionSpacing.elementVerticalPadding)
    )
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = SectionSpacing.elementVerticalPadding)
    ) {
        Checkbox(
            checked = isBSplinesEnabled,
            onCheckedChange = { value -> onValueChanged(value) }
        )
        Spacer(modifier = Modifier.size(SectionSpacing.elementHorizontalPadding))
        Text(text = "Draw locations in BSplines")
    }
}
