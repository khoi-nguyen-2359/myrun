package akio.apps.myrun.feature.userhome.ui

import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

@Composable
fun UserHome() {
    var count by remember { mutableStateOf(0) }
    ++count
    Text(text = "User's Home $count")
}
