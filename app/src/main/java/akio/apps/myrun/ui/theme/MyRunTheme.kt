package akio.apps.myrun.ui.theme

import androidx.compose.material.MaterialTheme
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

object MyRunAppColors {
    val primary = Color(0xff1565c0)
    val secondary = Color(0xff5e92f3)
    val onSecondary = Color(0xffffffff)
}

@Composable
fun MyRunAppTheme(content: @Composable () -> Unit) = MaterialTheme(
    colors = lightColors(
        primary = MyRunAppColors.primary,
        secondary = MyRunAppColors.secondary,
        onSecondary = MyRunAppColors.onSecondary
    ),
    content = content
)
