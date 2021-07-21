package akio.apps.myrun.feature.home

import akio.apps.myrun.data.activity.ActivityLocalStorage
import androidx.lifecycle.ViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged

class HomeViewModel @Inject constructor(
    private val activityLocalStorage: ActivityLocalStorage
) : ViewModel() {
    val activityStorageCount: Flow<Int> =
        activityLocalStorage.getActivityStorageDataCount().distinctUntilChanged()
}
