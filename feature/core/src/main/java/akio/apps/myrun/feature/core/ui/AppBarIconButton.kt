package akio.apps.myrun.feature.core.ui

import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

@Composable
fun AppBarIconButton(
    iconImageVector: ImageVector,
    contentDescription: String? = null,
    onClick: () -> Unit,
) = IconButton(onClick = onClick) {
    Icon(
        tint = Color.White,
        imageVector = iconImageVector,
        contentDescription = contentDescription
    )
}

@Composable
fun AppBarTextButton(
    text: String,
    onClick: () -> Unit,
) = IconButton(onClick = onClick) {
    Text(
        fontWeight = FontWeight.SemiBold,
        text = text.uppercase(),
        color = Color.White,
        fontSize = 14.sp
    )
}

@Composable
fun AppBarArrowBackButton(navController: NavController) =
    AppBarIconButton(iconImageVector = Icons.Rounded.ArrowBack) {
        navController.popBackStack()
    }
