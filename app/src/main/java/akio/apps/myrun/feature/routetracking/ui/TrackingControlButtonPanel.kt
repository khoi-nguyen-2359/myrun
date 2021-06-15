package akio.apps.myrun.feature.routetracking.ui

import akio.apps.myrun.R
import akio.apps.myrun.data.routetracking.RouteTrackingStatus
import akio.apps.myrun.feature.routetracking.RouteTrackingViewModel
import akio.apps.myrun.ui.theme.MyRunAppTheme
import android.location.Location
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.GpsNotFixed
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun TrackingControlButtonPanel(
    routeTrackingViewModel: RouteTrackingViewModel,
    onClickControlButton: (TrackingControlButtonType) -> Unit
) = MyRunAppTheme {
    val trackingStatus by routeTrackingViewModel.trackingStatus.observeAsState()
    val initialLocation by produceState<Location?>(
        initialValue = null,
        producer = { value = routeTrackingViewModel.getInitialLocation() }
    )
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp),
        horizontalArrangement = Arrangement.Center
    ) {
        if (initialLocation == null) {
            TrackingGpsSignalIcon()
        } else {
            val controlButtonType = mapTrackingStatusToControlButtonType[trackingStatus]
            val items = if (trackingStatus == RouteTrackingStatus.PAUSED) {
                listOfNotNull(controlButtonType, TrackingControlButtonType.Stop)
            } else {
                listOfNotNull(controlButtonType)
            }
            items.forEach { buttonType ->
                TrackingControlButton(
                    label = stringResource(id = buttonType.label),
                    color = buttonType.color,
                    onClickAction = { onClickControlButton(buttonType) }
                )
            }
        }
    }
}

private val mapTrackingStatusToControlButtonType = mapOf(
    RouteTrackingStatus.STOPPED to TrackingControlButtonType.Start,
    RouteTrackingStatus.RESUMED to TrackingControlButtonType.Pause,
    RouteTrackingStatus.PAUSED to TrackingControlButtonType.Resume
)

enum class TrackingControlButtonType(
    @StringRes val label: Int,
    val color: Color
) {
    Start(
        label = R.string.action_start,
        color = Color(0xff00c853)
    ),
    Pause(
        label = R.string.action_pause,
        color = Color(0xffb71c1c)
    ),
    Resume(
        label = R.string.action_resume,
        color = Start.color
    ),
    Stop(
        label = R.string.action_stop,
        color = Pause.color
    )

}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun CircularControlButton(
    color: Color,
    onClickAction: (() -> Unit)? = null,
    isClickable: Boolean = false,
    content: @Composable () -> Unit
) = Surface(
    elevation = 4.dp,
    modifier = Modifier
        .size(80.dp)
        .padding(4.dp),
    shape = RoundedCornerShape(CornerSize(40.dp)),
    color = color,
    onClick = { onClickAction?.invoke() },
    enabled = isClickable,
    content = content
)

@Composable
private fun TrackingGpsSignalIcon() = CircularControlButton(
    color = Color(0xfff57f17)
) {
    Box(contentAlignment = Alignment.Center) {
        Icon(
            imageVector = Icons.Rounded.GpsNotFixed,
            tint = Color.White,
            contentDescription = "GPS signal icon"
        )
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun TrackingControlButton(
    label: String,
    color: Color,
    onClickAction: () -> Unit,
) = CircularControlButton(
    color = color,
    onClickAction = onClickAction,
    isClickable = true
) {
    Box(contentAlignment = Alignment.Center) {
        Text(
            text = label.uppercase(),
            color = Color.White,
            fontSize = 15.sp,
            fontStyle = FontStyle.Italic,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
        )
    }
}

//@Composable
//@Preview
//private fun PreviewControlButton() = TrackingControlButton("Control Button", Color.Black) {}
