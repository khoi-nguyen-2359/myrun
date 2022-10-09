package akio.apps.myrun.feature.feed

import akio.apps.myrun.data.activity.api.ActivityLocalStorage
import akio.apps.myrun.feature.core.BaseViewModel
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import javax.inject.Inject
import kotlinx.coroutines.flow.distinctUntilChanged

class FeedViewModel @Inject constructor(
    private val activityLocalStorage: ActivityLocalStorage,
) : BaseViewModel() {

    @Composable
    fun getUploadingActivityCount(): Int = rememberFlow(
        flow = remember {
            activityLocalStorage.getActivityStorageDataCountFlow().distinctUntilChanged()
        },
        default = 0
    )
}
