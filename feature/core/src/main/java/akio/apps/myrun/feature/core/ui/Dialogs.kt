package akio.apps.myrun.feature.core.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.AlertDialog
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.ListItem
import androidx.compose.material.MaterialTheme
import androidx.compose.material.RadioButton
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.sharp.ErrorOutline
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
fun SingleChoiceListDialog(
    title: String,
    items: List<String>,
    selectedItem: String,
    onClickAtIndex: (Int) -> Unit,
    onDismiss: () -> Unit,
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties()
    ) {
        DialogContentContainer {
            Column(modifier = Modifier.selectableGroup()) {
                ListItem(
                    text = { Text(text = title, style = AppTextStyle.dialogTitle()) }
                )
                items.forEachIndexed { index, itemText ->
                    ListItem(
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                RadioButton(selected = itemText == selectedItem, onClick = null)
                                Spacer(modifier = Modifier.width(16.dp))
                                Text(itemText)
                            }
                        },
                        modifier = Modifier.selectable(
                            onClick = {
                                onClickAtIndex(index)
                                onDismiss()
                            },
                            role = Role.RadioButton,
                            selected = itemText == selectedItem
                        )

                    )
                }
            }
        }
    }
}

@Composable
private fun DialogContentContainer(content: @Composable BoxScope.() -> Unit) =
    Surface(shape = MaterialTheme.shapes.medium) {
        Box(
            modifier = Modifier.padding(vertical = AppDimensions.rowVerticalPadding),
            content = content
        )
    }

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun ProgressDialogContent(text: String) {
    DialogContentContainer {
        ListItem(
            icon = { CircularProgressIndicator() },
            text = { Text(text = text) }
        )
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun ErrorDialog(text: String, onDismiss: () -> Unit) {
    var isShowing by remember { mutableStateOf(true) }
    if (!isShowing) {
        return
    }
    Dialog(
        onDismissRequest = {
            isShowing = false
            onDismiss()
        }) {
        DialogContentContainer {
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

@Preview
@Composable
private fun PreviewProgressDialog() {
    ProgressDialogContent(text = "Super long long\nlong text")
}

@Composable
fun ConfirmationDialog(
    title: String,
    onDismiss: () -> Unit,
    confirmLabel: String,
    onConfirmed: () -> Unit,
    cancelLabel: String,
    onCanceled: () -> Unit = { },
    message: String,
) {
    AlertDialog(
        title = { Text(text = title, style = AppTextStyle.dialogTitle()) },
        onDismissRequest = onDismiss,
        buttons = {
            Row(
                horizontalArrangement = Arrangement.End,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = AppDimensions.rowVerticalPadding)
                    .padding(horizontal = AppDimensions.screenHorizontalPadding)
            ) {
                TextButton(
                    onClick = {
                        onCanceled()
                        onDismiss()
                    }
                ) {
                    Text(text = cancelLabel, fontSize = 18.sp)
                }
                TextButton(
                    onClick = {
                        onConfirmed()
                        onDismiss()
                    }
                ) {
                    Text(text = confirmLabel, fontSize = 18.sp)
                }
            }
        },
        text = { Text(text = message, fontSize = 16.sp) }
    )
}
