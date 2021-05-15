package akio.apps.myrun.feature.activitydetail

import akio.apps._base.Resource
import akio.apps.myrun.feature.usertimeline.model.Activity
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.Flow

abstract class ActivityDetailViewModel : ViewModel() {
    abstract val activityDetails: Flow<Resource<Activity>>
    abstract fun getActivityFormattedDateTime(): ActivityDateTimeFormatter.Result
    abstract suspend fun getActivityPlaceDisplayName(): String?
    abstract fun loadActivityDetails()

    class ActivityNotFoundException : Exception()

    data class Params(val activityId: String)
}
