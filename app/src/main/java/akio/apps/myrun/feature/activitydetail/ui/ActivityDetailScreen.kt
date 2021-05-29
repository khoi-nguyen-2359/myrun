package akio.apps.myrun.feature.activitydetail.ui

import akio.apps._base.Resource
import akio.apps.myrun.R
import akio.apps.myrun.feature.activitydetail.ActivityDetailViewModel
import akio.apps.myrun.ui.theme.MyRunAppTheme
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.accompanist.glide.rememberGlidePainter

@Composable
fun ActivityDetailScreen(
    activityDetailViewModel: ActivityDetailViewModel,
    onClickRouteImage: (encodedPolyline: String) -> Unit
) = MyRunAppTheme {
    val activityResource by activityDetailViewModel.activityDetails.collectAsState(
        Resource.Loading()
    )
    val activityDetail = activityResource.data
    if (activityDetail != null) {
        val activityDisplayPlaceName by produceState<String?>(initialValue = null) {
            value = activityDetailViewModel.getActivityPlaceDisplayName()
        }
        Column {
            Box {
                ActivityInfoHeaderComposable(
                    activityDetail,
                    activityDisplayPlaceName
                )
                if (activityResource is Resource.Loading) {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                }
            }
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
            PerformanceTableComposable(activityDetail)
        }
    } else if (activityResource is Resource.Loading) {
        FullscreenLoadingView()
    }
}

@Composable
private fun FullscreenLoadingView() = Column(
    modifier = Modifier
        .fillMaxWidth()
        .fillMaxHeight(),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.Center
) {
    CircularProgressIndicator()
    Spacer(modifier = Modifier.height(10.dp))
    Text(
        text = stringResource(id = R.string.activity_details_loading_message),
        color = colorResource(id = R.color.user_timeline_instruction_text),
        fontSize = 15.sp
    )
}
