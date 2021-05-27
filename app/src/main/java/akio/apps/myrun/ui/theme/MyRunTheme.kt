package akio.apps.myrun.ui.theme

import androidx.compose.material.MaterialTheme
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

@Composable
fun MyRunAppTheme(content: @Composable () -> Unit) = MaterialTheme(
    colors = lightColors(
        primary = Color(0xff1565c0),
        secondary = Color(0xff5e92f3),
        onSecondary = Color(0xffffffff)
    ),
    content = content
)
