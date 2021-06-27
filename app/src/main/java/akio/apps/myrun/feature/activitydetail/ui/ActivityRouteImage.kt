package akio.apps.myrun.feature.activitydetail.ui

import akio.apps.myrun.R
import akio.apps.myrun.feature.usertimeline.model.Activity
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import com.google.accompanist.coil.rememberCoilPainter
import com.google.accompanist.imageloading.ImageLoadState
import com.google.accompanist.placeholder.PlaceholderHighlight
import com.google.accompanist.placeholder.material.placeholder
import com.google.accompanist.placeholder.material.shimmer


@Composable
fun ActivityRouteImage(
    activity: Activity,
    onClickAction: (() -> Unit)? = null
) {
    val imagePainter = rememberCoilPainter(
        request = activity.routeImage,
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
            .aspectRatio(1.5f)
            .placeholder(
                visible = imagePainter.loadState !is ImageLoadState.Success,
                highlight = PlaceholderHighlight.shimmer()
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
