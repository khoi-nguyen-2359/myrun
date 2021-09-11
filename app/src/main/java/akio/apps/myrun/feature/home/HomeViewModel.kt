package akio.apps.myrun.feature.home

import akio.apps.myrun.data.activity.api.ActivityLocalStorage
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import javax.inject.Inject
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.shareIn

class HomeViewModel @Inject constructor(
    private val activityLocalStorage: ActivityLocalStorage,
) : ViewModel() {
    val activityUploadBadge: Flow<ActivityUploadBadgeStatus> =
        createActivityUploadBadgeStatusFlow()

    private var lastActivityStorageCount: Int = -1

    @OptIn(FlowPreview::class)
    private fun createActivityUploadBadgeStatusFlow(): Flow<ActivityUploadBadgeStatus> =
        activityLocalStorage.getActivityStorageDataCountFlow()
            .distinctUntilChanged()
            .mapNotNull { count ->
                val status = when {
                    count > 0 -> ActivityUploadBadgeStatus.InProgress(count)
                    count == 0 && lastActivityStorageCount > 0 -> ActivityUploadBadgeStatus.Complete
                    else -> null
                }
                lastActivityStorageCount = count

                status
            }
            // there are 2 collectors so use shareIn
            .shareIn(viewModelScope, replay = 1, started = SharingStarted.WhileSubscribed())

    sealed class ActivityUploadBadgeStatus {
        class InProgress(val activityCount: Int) : ActivityUploadBadgeStatus()
        object Complete : ActivityUploadBadgeStatus()
        object Hidden : ActivityUploadBadgeStatus()
    }
}
