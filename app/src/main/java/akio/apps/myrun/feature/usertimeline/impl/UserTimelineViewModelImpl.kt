package akio.apps.myrun.feature.usertimeline.impl

import akio.apps.myrun.feature.usertimeline.UserTimelineViewModel
import akio.apps.myrun.feature.usertimeline.model.Activity
import androidx.lifecycle.LiveData
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.liveData
import javax.inject.Inject

class UserTimelineViewModelImpl @Inject constructor(
    private val activityPagingSource: ActivityPagingSource
) : UserTimelineViewModel() {

    override val myActivityList: LiveData<PagingData<Activity>> = Pager(
        config = PagingConfig(
            pageSize = PAGE_SIZE,
            prefetchDistance = PAGE_SIZE / 2
        ),
        initialKey = System.currentTimeMillis()
    ) { activityPagingSource }.liveData

    companion object {
        const val PAGE_SIZE = 4
    }
}