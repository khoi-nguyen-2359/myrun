package akio.apps.myrun.feature.activitydetail.ui

import akio.apps.myrun.R
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
import com.google.accompanist.glide.rememberGlidePainter
import com.google.accompanist.imageloading.ImageLoadState
import com.google.accompanist.placeholder.PlaceholderHighlight
import com.google.accompanist.placeholder.material.fade
import com.google.accompanist.placeholder.material.placeholder

private const val ACTIVITY_ROUTE_IMAGE_RATIO = 1.5f

@Composable
fun ActivityRouteImage(
    activity: Activity,
    onClickAction: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val imageWidth = remember { context.resources.displayMetrics.widthPixels / 2 }
    val imageHeight = remember { (imageWidth / ACTIVITY_ROUTE_IMAGE_RATIO).toInt() }
    val imagePainter = rememberGlidePainter(
        request = activity.routeImage,
        requestBuilder = {
            override(imageWidth, imageHeight)
        },
        shouldRefetchOnSizeChange = { _, _ -> false },
        previewPlaceholder = R.drawable.ic_run_circle,
        fadeIn = true,
        fadeInDurationMs = 200
    )
    Image(
        painter = imagePainter,
        contentDescription = "Activity route image",
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(ACTIVITY_ROUTE_IMAGE_RATIO)
            .placeholder(
                visible = imagePainter.loadState !is ImageLoadState.Success,
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
