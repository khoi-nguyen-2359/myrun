package akio.apps.myrun.feature.base.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.ListItem
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.sharp.ErrorOutline
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

@Composable
fun ProgressDialog(text: String) {
    Dialog(
        onDismissRequest = { },
        properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)
    ) {
        ProgressDialogContent(text)
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun ProgressDialogContent(text: String) {
    Surface(shape = MaterialTheme.shapes.medium) {
        Box(modifier = Modifier.padding(vertical = AppDimensions.rowVerticalSpacing)) {
            ListItem(
                icon = { CircularProgressIndicator() },
                text = { Text(text = text) }
            )
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun ErrorDialog(text: String) {
    var isShowing by remember { mutableStateOf(true) }
    if (!isShowing) {
        return
    }
    Dialog(onDismissRequest = { isShowing = false }) {
        Surface(shape = MaterialTheme.shapes.medium) {
            Box(modifier = Modifier.padding(vertical = AppDimensions.rowVerticalSpacing)) {
                ListItem(
                    icon = {
                        Icon(
                            imageVector = Icons.Sharp.ErrorOutline,
                            contentDescription = "Error icon"
                        )
                    },
                    text = { Text(text = text) }
                )
            }
        }
    }
}

@Preview
@Composable
private fun PreviewProgressDialog() {
    ProgressDialogContent(text = "Super long long\nlong text")
}
