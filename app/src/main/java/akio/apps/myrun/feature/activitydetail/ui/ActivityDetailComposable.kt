package akio.apps.myrun.feature.activitydetail.ui

import akio.apps._base.Resource
import akio.apps.myrun.R
import akio.apps.myrun.domain.PerformanceUnit
import akio.apps.myrun.feature.activitydetail.ActivityDetailViewModel
import akio.apps.myrun.feature.activitydetail.ActivityPerformedResultFormatter
import akio.apps.myrun.feature.usertimeline.model.Activity
import akio.apps.myrun.feature.usertimeline.model.CyclingActivity
import akio.apps.myrun.feature.usertimeline.model.RunningActivity
import android.content.Context
import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import com.google.accompanist.glide.rememberGlidePainter
import timber.log.Timber

@Composable
fun ActivityDetailComposable(
    activityDetailViewModel: ActivityDetailViewModel,
    onClickRouteImage: (encodedPolyline: String) -> Unit
) {
    val activityResource by activityDetailViewModel.activityDetails.collectAsState(
        Resource.Loading()
    )
    val activityDetail = activityResource.data
    Timber.d("activityResource=$activityResource")
    if (activityDetail != null) {
        val activityDisplayPlaceName by produceState<String?>(initialValue = null) {
            value = activityDetailViewModel.getActivityPlaceDisplayName()
        }
        Column {
            ActivityInfoHeaderComposable(
                activityDetail,
                activityDisplayPlaceName
            )
            Image(
                painter = rememberGlidePainter(
                    request = activityDetail.routeImage,
                    shouldRefetchOnSizeChange = { _, _ -> false },
                ),
                contentDescription = "Activity route image",
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(ratio = 1.5f)
                    .clickable { onClickRouteImage(activityDetail.encodedPolyline) },
                contentScale = ContentScale.Crop,
            )
            PerformanceTableComposable(
                activityDetail,
                listOf(
                    ActivityPerformedResultFormatter.Distance,
                    ActivityPerformedResultFormatter.Pace,
                    ActivityPerformedResultFormatter.Speed,
                    ActivityPerformedResultFormatter.Duration
                )
            )
        }
    }
}


