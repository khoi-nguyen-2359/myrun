package akio.apps.myrun.feature.usertimeline.impl

import akio.apps.base.feature.lifecycle.Event
import akio.apps.base.feature.viewmodel.LaunchCatchingDelegate
import akio.apps.base.feature.viewmodel.LaunchCatchingDelegateImpl
import akio.apps.myrun.data.activity.ActivityLocalStorage
import akio.apps.myrun.data.authentication.UserAuthenticationState
import akio.apps.myrun.data.recentplace.PlaceIdentifier
import akio.apps.myrun.data.recentplace.UserRecentPlaceRepository
import akio.apps.myrun.domain.recentplace.CreateActivityDisplayPlaceNameUsecase
import akio.apps.myrun.feature.usertimeline.UserTimelineViewModel
import akio.apps.myrun.feature.usertimeline.model.Activity
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import javax.inject.Inject
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.launch

class UserTimelineViewModelImpl @Inject constructor(
    private val activityPagingSourceFactory: ActivityPagingSourceFactory,
    private val createActivityDisplayPlaceNameUsecase: CreateActivityDisplayPlaceNameUsecase,
    private val userRecentPlaceRepository: UserRecentPlaceRepository,
    private val userAuthenticationState: UserAuthenticationState,
    private val activityLocalStorage: ActivityLocalStorage,
    private val launchCatchingViewModel: LaunchCatchingDelegateImpl = LaunchCatchingDelegateImpl()
) : UserTimelineViewModel(), LaunchCatchingDelegate by launchCatchingViewModel {

    private var activityPagingSource: ActivityPagingSource? = null

    private var lastActivityStorageCount: Int = -1

    override val activityUploadBadge: Flow<ActivityUploadBadgeStatus> =
        createActivityUploadBadgeStatusFlow()

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

    override val myActivityList: Flow<PagingData<Activity>> = Pager(
        config = PagingConfig(
            pageSize = PAGE_SIZE,
            enablePlaceholders = false,
            prefetchDistance = PAGE_SIZE
        ),
        initialKey = System.currentTimeMillis()
    ) { recreateActivityPagingSource() }.flow

    private fun recreateActivityPagingSource(): ActivityPagingSource =
        activityPagingSourceFactory().also {
            activityPagingSource = it
        }

    private val mapActivityIdToPlaceName: MutableMap<String, String> = mutableMapOf()

    // This is optional data, null for not found.
    private var userRecentPlaceIdentifier: PlaceIdentifier? = null

    private val isLoadingInitialDataMutable: MutableStateFlow<Boolean> = MutableStateFlow(true)
    override val isLoadingInitialData: Flow<Boolean> = isLoadingInitialDataMutable

    init {
        loadInitialData()
        observeActivityUploadCount()
    }

    private fun observeActivityUploadCount() = viewModelScope.launch {
        activityUploadBadge.collect {
            reloadFeedData()
        }
    }

    override fun reloadFeedData() {
        activityPagingSource?.invalidate()
    }

    override fun getActivityDisplayPlaceName(activity: Activity): String {
        val activityPlaceIdentifier = activity.placeIdentifier ?: return ""
        val activityId = activity.id
        var placeName = mapActivityIdToPlaceName[activityId]
        if (placeName != null) {
            return placeName
        }

        placeName = createActivityDisplayPlaceNameUsecase(
            activityPlaceIdentifier,
            userRecentPlaceIdentifier
        ) ?: ""
        mapActivityIdToPlaceName[activityId] = placeName

        return placeName
    }

    private fun loadInitialData() =
        viewModelScope.launchCatching(
            progressStateFlow = isLoadingInitialDataMutable,
            errorStateFlow = MutableStateFlow(Event(null))
        ) {
            val userId = userAuthenticationState.getUserAccountId()
            if (userId != null) {
                userRecentPlaceIdentifier =
                    userRecentPlaceRepository.getRecentPlaceIdentifier(userId)
            }
        }

    companion object {
        const val PAGE_SIZE = 6
    }
}
