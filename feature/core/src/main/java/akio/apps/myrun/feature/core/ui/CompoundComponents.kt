package akio.apps.myrun.feature.core.ui

import akio.apps.myrun.feature.core.ui.CompoundComponentsDimens.contentSpacer
import akio.apps.myrun.feature.core.ui.CompoundComponentsDimens.iconSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.Checkbox
import androidx.compose.material.Icon
import androidx.compose.material.LocalContentAlpha
import androidx.compose.material.LocalContentColor
import androidx.compose.material.Switch
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private object CompoundComponentsDimens {
    val contentSpacer = 16.dp
    val iconSize = 24.dp
}

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
fun CompoundCheckBox(
    label: String,
    checked: Boolean,
    onClick: () -> Unit,
) = Row(
    verticalAlignment = CenterVertically,
    modifier = Modifier
        .clickable { onClick() }
        .addCompoundComponentPaddings()
) {
    Checkbox(checked = checked, onCheckedChange = null)
    Spacer(modifier = Modifier.width(contentSpacer))
    Text(text = label, maxLines = 2, overflow = TextOverflow.Ellipsis)
}

@Composable
fun CompoundSwitch(
    label: String,
    checked: Boolean,
    enabled: Boolean,
    startIcon: ImageVector? = null,
    onClick: () -> Unit,
) = Row(
    verticalAlignment = CenterVertically,
    modifier = Modifier
        .modifyIf(enabled) {
            clickable { onClick() }
        }
        .addCompoundComponentPaddings()
) {
    if (startIcon != null) {
        StartIcon(startIcon)
        Spacer(modifier = Modifier.width(contentSpacer))
    }
    Text(
        text = label,
        modifier = Modifier.weight(1f),
        maxLines = 2,
        overflow = TextOverflow.Ellipsis
    )
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
    tint: Color? = null,
    value: String? = null,
    icon: ImageVector? = null,
    onClick: () -> Unit,
) = Row(
    modifier = Modifier
        .clickable { onClick() }
        .addCompoundComponentPaddings()
) {
    if (icon != null) {
        StartIcon(icon, tint)
        Spacer(modifier = Modifier.width(contentSpacer))
    }

    Column(modifier = Modifier.align(CenterVertically)) {
        val labelMaxLines = if (value == null) {
            2
        } else {
            1
        }
        Text(
            text = label,
            modifier = Modifier.fillMaxWidth(),
            maxLines = labelMaxLines,
            overflow = TextOverflow.Ellipsis,
            color = tint ?: Color.Unspecified
        )
        if (value != null) {
            Text(
                text = value,
                modifier = Modifier.fillMaxWidth(),
                fontSize = 13.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun Modifier.addCompoundComponentPaddings() = this.padding(
    vertical = AppDimensions.rowVerticalPadding,
    horizontal = AppDimensions.screenHorizontalPadding
)

@Composable
private fun RowScope.StartIcon(icon: ImageVector, tint: Color? = null) {
    Icon(
        icon,
        null,
        modifier = Modifier
            .size(iconSize)
            .align(CenterVertically),
        tint = tint ?: LocalContentColor.current.copy(alpha = LocalContentAlpha.current)
    )
}

@Preview
@Composable
private fun PreviewCompoundText() = CompoundText(label = "label", value = "value") {}
