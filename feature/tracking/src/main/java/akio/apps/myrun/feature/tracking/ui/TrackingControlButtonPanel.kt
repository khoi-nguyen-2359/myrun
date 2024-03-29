package akio.apps.myrun.feature.tracking.ui

import akio.apps.myrun.data.location.api.model.Location
import akio.apps.myrun.data.tracking.api.model.RouteTrackingStatus
import akio.apps.myrun.feature.core.ui.AppColors
import akio.apps.myrun.feature.core.ui.AppTheme
import akio.apps.myrun.feature.tracking.R
import akio.apps.myrun.feature.tracking.RouteTrackingActivity
import akio.apps.myrun.feature.tracking.RouteTrackingViewModel
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
import androidx.compose.material.icons.outlined.ZoomOutMap
import androidx.compose.material.icons.rounded.GpsOff
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
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
internal fun TrackingControlButtonPanel(
    routeTrackingViewModel: RouteTrackingViewModel,
    onClickControlButton: (TrackingControlButtonType) -> Unit,
    onClickCameraMode: (RouteTrackingActivity.CameraMovement) -> Unit,
) {
    val trackingStatus by routeTrackingViewModel.trackingStatus
        .collectAsState(initial = RouteTrackingStatus.STOPPED)
    // when entering the screen, initial location may not be available if location is not ready yet,
    // so use flow to get the location when it is ready.
    val initialLocation by produceState<Location?>(initialValue = null, producer = {
        value = routeTrackingViewModel.getLastLocation()
    })
    val stickyCameraMode by routeTrackingViewModel.stickyCameraButtonState.collectAsState()
    TrackingControlButtonPanel(
        stickyCameraMode,
        initialLocation,
        trackingStatus,
        onClickControlButton,
        onClickCameraMode
    )
}

@Composable
private fun TrackingControlButtonPanel(
    cameraMovement: RouteTrackingActivity.CameraMovement,
    initialLocation: Location?,
    @RouteTrackingStatus trackingStatus: Int?,
    onClickControlButton: (TrackingControlButtonType) -> Unit,
    onClickMyLocation: (RouteTrackingActivity.CameraMovement) -> Unit,
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
        StickyCameraModeButton(
            cameraMovement,
            onClickMyLocation,
            Modifier.align(Alignment.CenterEnd)
        )
    }
}

@Composable
private fun StickyCameraModeButton(
    cameraMovement: RouteTrackingActivity.CameraMovement,
    onClickMyLocation: (RouteTrackingActivity.CameraMovement) -> Unit,
    modifier: Modifier = Modifier,
) {
    val icon = when (cameraMovement) {
        RouteTrackingActivity.CameraMovement.StickyLocation -> Icons.Outlined.MyLocation
        RouteTrackingActivity.CameraMovement.StickyBounds -> Icons.Outlined.ZoomOutMap
        RouteTrackingActivity.CameraMovement.None -> return
    }
    Button(
        shape = CircleShape,
        onClick = { onClickMyLocation(cameraMovement) },
        modifier = modifier
            .padding(end = 16.dp)
            .size(48.dp),
        contentPadding = PaddingValues(4.dp),
        colors = ButtonDefaults.buttonColors(backgroundColor = Color.White)
    ) {
        Icon(
            imageVector = icon,
            tint = AppColors.primary,
            contentDescription = "Jump to my location on map"
        )
    }
}

private val mapTrackingStatusToControlButtonType = mapOf(
    RouteTrackingStatus.STOPPED to TrackingControlButtonType.Start,
    RouteTrackingStatus.RESUMED to TrackingControlButtonType.Pause,
    RouteTrackingStatus.PAUSED to TrackingControlButtonType.Resume
)

enum class TrackingControlButtonType(
    @StringRes val label: Int,
    val color: Color,
) {
    Start(
        label = R.string.action_start,
        color = AppColors.available
    ),
    Pause(
        label = R.string.action_pause,
        color = AppColors.destructive
    ),
    Resume(
        label = R.string.action_resume,
        color = AppColors.available
    ),
    Stop(
        label = R.string.action_stop,
        color = AppColors.destructive
    ),
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun CircularControlButton(
    color: Color,
    onClickAction: (() -> Unit)? = null,
    isClickable: Boolean = false,
    content: @Composable () -> Unit,
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
    RouteTrackingActivity.CameraMovement.StickyBounds,
    initialLocation = Location(0, 1, 2.0, 3.0, 4.0, 5.0),
    trackingStatus = RouteTrackingStatus.STOPPED,
    {},
    {}
)
