package akio.apps.myrun.feature.base.ui

import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

@Composable
fun AppBarIconButton(
    iconImageVector: ImageVector,
    contentDescription: String? = null,
    onClick: () -> Unit,
) =
    IconButton(onClick = onClick) {
        Icon(
            tint = Color.White,
            imageVector = iconImageVector,
            contentDescription = contentDescription
        )
    }
