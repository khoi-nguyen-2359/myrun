package akio.apps.myrun.feature.usertimeline

import akio.apps._base.viewmodel.BaseViewModel
import akio.apps.myrun.feature.usertimeline.model.Activity
import androidx.lifecycle.LiveData
import androidx.paging.PagingData

abstract class UserTimelineViewModel: BaseViewModel() {
    abstract val myActivityList: LiveData<PagingData<Activity>>
}