package akio.apps.myrun.feature.activitydetail.ui

import akio.apps._base.Resource
import akio.apps.myrun.feature.activitydetail.ActivityDateTimeFormatter
import akio.apps.myrun.feature.activitydetail.ActivityDetailsViewModel
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue

@Composable
fun ActivityDetailComposable(activityDetailsViewModel: ActivityDetailsViewModel) {
    val activityResource by activityDetailsViewModel.activityDetails.collectAsState(
        initial = Resource.Loading()
    )
    val activityFormattedStartTime by activityDetailsViewModel.activityDateTime.collectAsState(
        initial = ActivityDateTimeFormatter.Result.FullDateTime("")
    )
    val activity = activityResource.data
    if (activity != null) {
        ActivityInfoHeaderComposable(
            activity.athleteInfo,
            activityFormattedStartTime,
            activity.name
        )
    }
}
