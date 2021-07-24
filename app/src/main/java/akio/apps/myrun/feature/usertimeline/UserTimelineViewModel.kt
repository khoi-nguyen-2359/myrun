package akio.apps.myrun.feature.usertimeline

import akio.apps.myrun.feature.usertimeline.model.Activity
import androidx.lifecycle.ViewModel
import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow

abstract class UserTimelineViewModel : ViewModel() {
    abstract val myActivityList: Flow<PagingData<Activity>>
    abstract val isLoadingInitialData: Flow<Boolean>

    /**
     * Returns display text for activity place name, base on user's current place. Returns null if
     * activity doesn't have a place name.
     */
    abstract fun getActivityDisplayPlaceName(activity: Activity): String
}
