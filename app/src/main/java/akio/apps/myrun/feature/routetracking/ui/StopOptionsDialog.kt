package akio.apps.myrun.feature.routetracking.ui

import akio.apps.myrun.R
import akio.apps.myrun.feature.routetracking.RouteTrackingViewModel
import akio.apps.myrun.ui.theme.AppTheme
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.ListItem
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Save
import androidx.compose.material.icons.rounded.TransitEnterexit
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.window.Dialog

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun StopOptionsDialog(
    routeTrackingViewModel: RouteTrackingViewModel,
    itemSelectAction: (StopDialogOptionId) -> Unit
) = AppTheme {
    val isShowingState by routeTrackingViewModel.isStopOptionDialogShowing
        .collectAsState(initial = false)
    if (!isShowingState) {
        return@AppTheme
    }

    val items = listOf(
        Triple(StopDialogOptionId.Save, Icons.Rounded.Save, R.string.route_tracking_save_activity),
        Triple(
            StopDialogOptionId.Discard,
            Icons.Rounded.Delete,
            R.string.route_tracking_discard_activity
        ),
        Triple(StopDialogOptionId.Cancel, Icons.Rounded.TransitEnterexit, R.string.action_close)
    )
    Dialog(
        onDismissRequest = { routeTrackingViewModel.isStopOptionDialogShowing.value = false }
    ) {
        Surface(shape = MaterialTheme.shapes.medium) {
            Column {
                items.forEach { item ->
                    val (itemId, icon, label) = item
                    ListItem(
                        icon = {
                            Icon(
                                imageVector = icon,
                                contentDescription = stringResource(id = label)
                            )
                        },
                        text = { Text(text = stringResource(id = label)) },
                        modifier = Modifier.clickable {
                            routeTrackingViewModel.isStopOptionDialogShowing.value = false
                            itemSelectAction(itemId)
                        }
                    )
                }
            }
        }
    }
}

enum class StopDialogOptionId {
    Save, Discard, Cancel
}
