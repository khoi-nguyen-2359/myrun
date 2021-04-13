package akio.apps.myrun.feature.activitydetail.ui

import akio.apps._base.Resource
import akio.apps.myrun.feature.activitydetail.ActivityDateTimeFormatter
import akio.apps.myrun.feature.activitydetail.ActivityDetailViewModel
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import com.google.accompanist.glide.GlideImage

@Composable
fun ActivityDetailComposable(activityDetailViewModel: ActivityDetailViewModel) {
    val activityResource by activityDetailViewModel.activityDetails.collectAsState(
        Resource.Loading()
    )
    val activityFormattedStartTime by activityDetailViewModel.activityDateTime.collectAsState(
        ActivityDateTimeFormatter.Result.FullDateTime("")
    )

    val activity = activityResource.data
    if (activity != null) {
        Column {
            ActivityInfoHeaderComposable(activity, activityFormattedStartTime)
            GlideImage(
                data = activity.routeImage,
                contentDescription = "Activity route image",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(ratio = 1.5f)
            )
            //            PerformanceTableComposable(performedResults = )
        }
    }
}
