package akio.apps.myrun.feature.activitydetail.ui

import akio.apps.myrun.data.activity.api.model.ActivityDataModel
import akio.apps.myrun.data.activity.api.model.ActivityType
import akio.apps.myrun.data.activity.api.model.AthleteInfo
import akio.apps.myrun.data.activity.api.model.BaseActivityModel
import akio.apps.myrun.data.activity.api.model.RunningActivityModel
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import coil.compose.AsyncImagePainter
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.google.accompanist.placeholder.PlaceholderHighlight
import com.google.accompanist.placeholder.material.placeholder
import com.google.accompanist.placeholder.material.shimmer

private const val ACTIVITY_ROUTE_IMAGE_RATIO = 1.5f

@OptIn(coil.annotation.ExperimentalCoilApi::class)
@Composable
internal fun ActivityRouteImage(
    activity: BaseActivityModel,
    imageRatio: Float = ACTIVITY_ROUTE_IMAGE_RATIO,
    onClickAction: (() -> Unit)? = null,
) {
    val context = LocalContext.current
    val imageWidth = remember { (context.resources.displayMetrics.widthPixels * 0.8).toInt() }
    val imageHeight = remember { (imageWidth / imageRatio).toInt() }
    val imagePainter = rememberAsyncImagePainter(
        ImageRequest.Builder(LocalContext.current)
            .data(data = activity.routeImage)
            .apply {
                size(imageWidth, imageHeight)
                    .crossfade(200)
            }
            .build()
    )
    Image(
        painter = imagePainter,
        contentDescription = "Activity route image",
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(imageRatio)
            .placeholder(
                visible = imagePainter.state !is AsyncImagePainter.State.Success,
                highlight = PlaceholderHighlight.shimmer()
            )
            .run {
                if (onClickAction != null) {
                    clickable { onClickAction() }
                } else {
                    this
                }
            },
        contentScale = ContentScale.Crop
    )
}

@Preview
@Composable
private fun PreviewActivityRouteImage() = ActivityRouteImage(
    activity = RunningActivityModel(
        activityData = ActivityDataModel(
            id = "id",
            activityType = ActivityType.Running,
            name = "Evening Run",
            routeImage = "http://example.com",
            placeIdentifier = null,
            startTime = System.currentTimeMillis(),
            endTime = 2000L,
            duration = 1000L,
            distance = 100.0,
            encodedPolyline = "",
            athleteInfo = AthleteInfo(
                userId = "id",
                userName = "Khoi Nguyen",
                userAvatar = "userAvatar"
            )
        ),
        pace = 1.0,
        cadence = 160
    )
)
