package akio.apps.myrun.ui.theme

import androidx.compose.material.MaterialTheme
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

object AppColors {
    val primary = Color(0xff1565c0)
    val primaryDark = Color(0xff003c8f)
    val secondary = Color(0xff5e92f3)
    val onSecondary = Color(0xffffffff)
}

object AppDimensions {
    val screenHorizontalPadding: Dp = 16.dp
}

@Composable
fun AppTheme(content: @Composable () -> Unit) = MaterialTheme(
    colors = lightColors(
        primary = AppColors.primary,
        secondary = AppColors.secondary,
        onSecondary = AppColors.onSecondary
    ),
    content = content,
    typography = MaterialTheme.typography
)
