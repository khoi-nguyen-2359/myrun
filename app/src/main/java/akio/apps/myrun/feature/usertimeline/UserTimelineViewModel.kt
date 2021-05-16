package akio.apps.myrun.feature.usertimeline

import akio.apps.myrun.feature.usertimeline.model.Activity
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow

abstract class UserTimelineViewModel : ViewModel() {
    abstract val myActivityList: Flow<PagingData<Activity>>

    /**
     * Returns display text for activity place name. Returns null if activity doesn't have a place
     * name.
     */
    abstract suspend fun mapActivityToActivityDisplayPlaceName(activity: Activity): String?
}
