package akio.apps.myrun.feature.activitydetail.ui

import akio.apps.myrun.feature.usertimeline.model.Activity
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import coil.compose.ImagePainter
import coil.compose.rememberImagePainter
import com.google.accompanist.placeholder.PlaceholderHighlight
import com.google.accompanist.placeholder.material.fade
import com.google.accompanist.placeholder.material.placeholder

private const val ACTIVITY_ROUTE_IMAGE_RATIO = 1.5f

@OptIn(coil.annotation.ExperimentalCoilApi::class)
@Composable
fun ActivityRouteImage(
    activity: Activity,
    onClickAction: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val imageWidth = remember { context.resources.displayMetrics.widthPixels / 2 }
    val imageHeight = remember { (imageWidth / ACTIVITY_ROUTE_IMAGE_RATIO).toInt() }
    val imagePainter = rememberImagePainter(
        data = activity.routeImage,
        builder = {
            size(imageWidth, imageHeight)
            crossfade(200)
        }
    )
    Image(
        painter = imagePainter,
        contentDescription = "Activity route image",
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(ACTIVITY_ROUTE_IMAGE_RATIO)
            .placeholder(
                visible = imagePainter.state !is ImagePainter.State.Success,
                highlight = PlaceholderHighlight.fade()
            )
            .run {
                if (onClickAction != null) {
                    clickable { onClickAction() }
                } else {
                    this
                }
            },
        contentScale = ContentScale.Crop,
    )
}
