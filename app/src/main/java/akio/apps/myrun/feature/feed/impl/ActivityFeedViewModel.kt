package akio.apps.myrun.feature.feed.impl

import akio.apps.common.data.Event
import akio.apps.common.data.LaunchCatchingDelegate
import akio.apps.myrun.data.activity.api.ActivityLocalStorage
import akio.apps.myrun.data.activity.api.model.ActivityModel
import akio.apps.myrun.data.authentication.api.UserAuthenticationState
import akio.apps.myrun.data.user.api.PlaceIdentifier
import akio.apps.myrun.data.user.api.UserRecentPlaceRepository
import akio.apps.myrun.domain.recentplace.MakeActivityPlaceNameUsecase
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
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
import timber.log.Timber

class ActivityFeedViewModel @Inject constructor(
    private val activityPagingSourceFactory: ActivityPagingSourceFactory,
    private val makeActivityPlaceNameUsecase: MakeActivityPlaceNameUsecase,
    private val userRecentPlaceRepository: UserRecentPlaceRepository,
    private val userAuthenticationState: UserAuthenticationState,
    private val activityLocalStorage: ActivityLocalStorage,
    private val launchCatchingViewModel: LaunchCatchingDelegate,
) : ViewModel(), LaunchCatchingDelegate by launchCatchingViewModel {

    private var activityPagingSource: ActivityPagingSource? = null

    private var lastActivityStorageCount: Int = -1

    val activityUploadBadge: Flow<ActivityUploadBadgeStatus> =
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

    val myActivityList: Flow<PagingData<ActivityModel>> = Pager(
        config = PagingConfig(
            pageSize = PAGE_SIZE,
            enablePlaceholders = false,
            prefetchDistance = PAGE_SIZE
        ),
        initialKey = System.currentTimeMillis()
    ) { recreateActivityPagingSource() }
        .flow
        .cachedIn(viewModelScope)

    private fun recreateActivityPagingSource(): ActivityPagingSource =
        activityPagingSourceFactory().also {
            Timber.d("recreateActivityPagingSource")
            activityPagingSource = it
        }

    private val mapActivityIdToPlaceName: MutableMap<String, String> = mutableMapOf()

    // This is optional data, null for not found.
    private var userRecentPlaceIdentifier: PlaceIdentifier? = null

    private val isLoadingInitialDataMutable: MutableStateFlow<Boolean> = MutableStateFlow(true)
    val isLoadingInitialData: Flow<Boolean> = isLoadingInitialDataMutable

    init {
        loadInitialData()
        observeActivityUploadCount()
    }

    private fun observeActivityUploadCount() = viewModelScope.launch {
        activityUploadBadge.collect {
            reloadFeedData()
        }
    }

    private fun reloadFeedData() {
        Timber.d("reloadFeedData")
        activityPagingSource?.invalidate()
    }

    fun getActivityDisplayPlaceName(activity: ActivityModel): String {
        val activityPlaceIdentifier = activity.placeIdentifier ?: return ""
        val activityId = activity.id
        var placeName = mapActivityIdToPlaceName[activityId]
        if (placeName != null) {
            return placeName
        }

        placeName = makeActivityPlaceNameUsecase(
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

    sealed class ActivityUploadBadgeStatus {
        class InProgress(val activityCount: Int) : ActivityUploadBadgeStatus()
        object Complete : ActivityUploadBadgeStatus()
        object Hidden : ActivityUploadBadgeStatus()
    }

    companion object {
        const val PAGE_SIZE = 6
    }
}