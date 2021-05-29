package akio.apps.myrun.feature.activitydetail.ui

import akio.apps._base.Resource
import akio.apps.myrun.R
import akio.apps.myrun.feature.activitydetail.ActivityDetailViewModel
import akio.apps.myrun.feature.usertimeline.model.Activity
import akio.apps.myrun.ui.theme.MyRunAppTheme
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Snackbar
import androidx.compose.material.SnackbarData
import androidx.compose.material.SnackbarDuration
import androidx.compose.material.SnackbarHost
import androidx.compose.material.SnackbarResult
import androidx.compose.material.Text
import androidx.compose.material.contentColorFor
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.accompanist.glide.rememberGlidePainter
import kotlinx.coroutines.launch
import akio.apps.myrun.feature.activitydetail.ui.ActivityInfoHeaderView as ActivityInfoHeaderView1

@Composable
fun ActivityDetailScreen(
    activityDetailViewModel: ActivityDetailViewModel,
    onClickRouteImage: (encodedPolyline: String) -> Unit
) = MyRunAppTheme {
    val scaffoldState = rememberScaffoldState()
    val coroutineScope = rememberCoroutineScope()
    val activityResource by activityDetailViewModel.activityDetails.collectAsState(
        Resource.Loading()
    )
    Scaffold(
        scaffoldState = scaffoldState,
        snackbarHost = { hostState ->
            SnackbarHost(hostState) { snackbarData ->
                LoadingErrorSnackbar(snackbarData)
            }
        }
    ) {
        val activityDetail = activityResource.data
        if (activityDetail != null) {
            val activityDisplayPlaceName by produceState<String?>(initialValue = null) {
                value = activityDetailViewModel.getActivityPlaceDisplayName()
            }
            Column {
                ActivityInfoHeaderView1(activityDetail, activityDisplayPlaceName)
                RouteImageView(activityDetail, onClickRouteImage)
                PerformanceTableComposable(activityDetail)
                if (activityResource is Resource.Loading) {
                    BottomLoadingIndicator()
                }
            }
        } else if (activityResource is Resource.Loading) {
            FullscreenLoadingView()
        }

        if (activityResource is Resource.Error) {
            val errorMessage = stringResource(id = R.string.activity_details_loading_error)
            val retryLabel = stringResource(id = R.string.action_retry)
            coroutineScope.launch {
                val snackbarResult = scaffoldState.snackbarHostState.showSnackbar(
                    message = errorMessage,
                    actionLabel = retryLabel,
                    duration = SnackbarDuration.Long
                )

                if (snackbarResult == SnackbarResult.ActionPerformed) {
                    activityDetailViewModel.loadActivityDetails()
                }
            }
        }
    }
}

@Composable
private fun ColumnScope.BottomLoadingIndicator() = Box(
    modifier = Modifier.weight(1.0f),
    contentAlignment = Alignment.BottomCenter
) {
    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
}

@Composable
private fun RouteImageView(
    activityDetail: Activity,
    onClickRouteImage: (encodedPolyline: String) -> Unit
) = Image(
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

@Composable
private fun LoadingErrorSnackbar(snackbarData: SnackbarData) {
    val snackbarBackgroundColor = MaterialTheme.colors.error
    val snackbarContentColor = contentColorFor(backgroundColor = snackbarBackgroundColor)
    Snackbar(
        backgroundColor = snackbarBackgroundColor,
        contentColor = snackbarContentColor,
        actionColor = snackbarContentColor,
        snackbarData = snackbarData
    )
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
