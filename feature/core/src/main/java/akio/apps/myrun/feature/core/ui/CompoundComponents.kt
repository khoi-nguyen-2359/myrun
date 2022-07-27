package akio.apps.myrun.feature.core.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Switch
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp

@Composable
fun FormSectionSpace() {
    Spacer(modifier = Modifier.height(AppDimensions.sectionVerticalSpacing))
}

inline fun Modifier.modifyIf(enabled: Boolean, application: Modifier.() -> Modifier): Modifier =
    this.run {
        if (enabled) {
            this.application()
        } else {
            this
        }
    }

@Composable
fun CompoundSwitch(
    label: String,
    checked: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
) = Row(
    verticalAlignment = Alignment.CenterVertically,
    modifier = Modifier
        .modifyIf(enabled) {
            clickable { onClick() }
        }
        .padding(
            vertical = AppDimensions.rowVerticalPadding,
            horizontal = AppDimensions.screenHorizontalPadding
        )
) {
    Text(text = label, modifier = Modifier.weight(1f))
    Switch(enabled = enabled, checked = checked, onCheckedChange = null)
}

@Preview
@Composable
private fun PreviewUserProfileSwitch() {
    CompoundSwitch("label", checked = true, enabled = true) {}
}

@Composable
fun CompoundText(
    label: String,
    value: String,
) = Column(
    modifier = Modifier.padding(
        vertical = AppDimensions.rowVerticalPadding,
        horizontal = AppDimensions.screenHorizontalPadding
    )
) {
    Text(
        text = label,
        modifier = Modifier.fillMaxWidth()
    )
    Text(text = value, modifier = Modifier.fillMaxWidth(), fontSize = 13.sp)
}

@Preview
@Composable
private fun PreviewCompoundText() = CompoundText(label = "label", value = "value")
