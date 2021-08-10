package akio.apps.myrun.feature.routetracking.ui

import akio.apps.myrun.R
import akio.apps.myrun.data.location.api.model.Location
import akio.apps.myrun.data.tracking.api.RouteTrackingStatus
import akio.apps.myrun.feature.routetracking.RouteTrackingViewModel
import akio.apps.myrun.ui.theme.AppColors
import akio.apps.myrun.ui.theme.AppTheme
import androidx.annotation.StringRes
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.MyLocation
import androidx.compose.material.icons.rounded.GpsOff
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val CIRCULAR_CONTROL_BUTTON_SIZE = 90.dp

@Composable
fun TrackingControlButtonPanel(
    routeTrackingViewModel: RouteTrackingViewModel,
    onClickControlButton: (TrackingControlButtonType) -> Unit,
    onClickMyLocation: () -> Unit
) {
    val trackingStatus by routeTrackingViewModel.trackingStatus.observeAsState()
    // when entering the screen, initial location may not be available if location is not ready yet,
    // so use flow to get the location when it is ready.
    val initialLocation by routeTrackingViewModel.getLastLocationFlow()
        .collectAsState(initial = null)
    TrackingControlButtonPanel(
        initialLocation,
        trackingStatus,
        onClickControlButton,
        onClickMyLocation
    )
}

@Composable
private fun TrackingControlButtonPanel(
    initialLocation: Location?,
    @RouteTrackingStatus trackingStatus: Int?,
    onClickControlButton: (TrackingControlButtonType) -> Unit,
    onClickMyLocation: () -> Unit
) = AppTheme {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp)
    ) {
        Row(
            modifier = Modifier.animateContentSize(),
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
        Button(
            shape = CircleShape,
            onClick = onClickMyLocation,
            modifier = Modifier
                .padding(end = 16.dp)
                .size(48.dp)
                .align(Alignment.CenterEnd),
            contentPadding = PaddingValues(4.dp),
            colors = ButtonDefaults.buttonColors(backgroundColor = Color.White)
        ) {
            Icon(
                imageVector = Icons.Outlined.MyLocation,
                tint = AppColors.primary,
                contentDescription = "Jump to my location on map"
            )
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
    elevation = 2.dp,
    modifier = Modifier
        .size(CIRCULAR_CONTROL_BUTTON_SIZE)
        .padding(8.dp),
    shape = RoundedCornerShape(CornerSize(CIRCULAR_CONTROL_BUTTON_SIZE / 2)),
    color = color,
    onClick = { onClickAction?.invoke() },
    enabled = isClickable,
    content = content
)

@OptIn(ExperimentalAnimationApi::class)
@Composable
private fun TrackingGpsSignalIcon() = CircularControlButton(
    color = Color(0xfff57f17)
) {
    val infiniteTransition = rememberInfiniteTransition()
    val animatedAlpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1500)
        )
    )
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.graphicsLayer {
            alpha = animatedAlpha
        }
    ) {
        Icon(
            imageVector = Icons.Rounded.GpsOff,
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
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            text = label.uppercase(),
            color = Color.White,
            fontSize = 15.sp,
            fontStyle = FontStyle.Italic,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
@Preview
private fun PreviewControlButton() = TrackingControlButton("Resume", Color.Black) {}

@Composable
@Preview
private fun PreviewTrackingControlButtonPanel() = TrackingControlButtonPanel(
    initialLocation = Location(1, 2.0, 3.0, 4.0, 5.0),
    trackingStatus = RouteTrackingStatus.STOPPED,
    {},
    {}
)
