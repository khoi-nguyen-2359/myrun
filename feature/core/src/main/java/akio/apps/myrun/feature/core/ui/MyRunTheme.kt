package akio.apps.myrun.feature.core.ui

import androidx.compose.material.MaterialTheme
import androidx.compose.material.lightColors
import androidx.compose.material.primarySurface
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

object AppColors {
    val primary = Color(0xff1565c0)
    val primaryDark = Color(0xff003c8f)
    val secondary = Color(0xff5e92f3)
    val onSecondary = Color(0xffffffff)
    val onPrimary = Color(0xffffffff)

    val available = Color(0xff00c853)
    val destructive = Color(0xffb71c1c)

    @Composable
    fun error() = MaterialTheme.colors.error

    @Composable
    fun primarySurface() = MaterialTheme.colors.primarySurface
}

object AppDimensions {
    val AppBarHeight = 56.dp
    val FabSize = 56.dp

    val iconButtonPadding = 16.dp
    val screenVerticalSpacing: Dp = 16.dp
    val screenHorizontalPadding: Dp = 16.dp
    val sectionVerticalSpacing: Dp = 16.dp
    val rowVerticalPadding: Dp = 12.dp
}

object AppTextStyle {
    @Composable
    fun dialogTitle() = MaterialTheme.typography.h6
}

@Composable
fun AppTheme(content: @Composable () -> Unit) = MaterialTheme(
    colors = lightColors(
        primary = AppColors.primary,
        secondary = AppColors.secondary,
        onSecondary = AppColors.onSecondary,
        onPrimary = AppColors.onPrimary
    ),
    typography = MaterialTheme.typography,
    content = content
)
