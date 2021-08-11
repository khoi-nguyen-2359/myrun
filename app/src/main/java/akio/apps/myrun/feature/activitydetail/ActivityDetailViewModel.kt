package akio.apps.myrun.feature.activitydetail

import akio.apps.common.data.Resource
import akio.apps.myrun.feature.usertimeline.model.Activity
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.Flow

abstract class ActivityDetailViewModel : ViewModel() {
    abstract val activityDetails: Flow<akio.apps.common.data.Resource<Activity>>
    abstract val activityPlaceName: Flow<String?>
    abstract fun loadActivityDetails()

    class ActivityNotFoundException : Exception()

    data class Params(val activityId: String)
}
