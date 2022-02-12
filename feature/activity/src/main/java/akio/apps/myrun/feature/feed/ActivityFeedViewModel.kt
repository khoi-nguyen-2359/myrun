package akio.apps.myrun.feature.feed

import akio.apps.myrun.base.di.NamedIoDispatcher
import akio.apps.myrun.data.activity.api.ActivityLocalStorage
import akio.apps.myrun.data.activity.api.model.BaseActivityModel
import akio.apps.myrun.data.authentication.api.UserAuthenticationState
import akio.apps.myrun.data.user.api.PlaceIdentifier
import akio.apps.myrun.data.user.api.UserPreferences
import akio.apps.myrun.data.user.api.UserRecentPlaceRepository
import akio.apps.myrun.data.user.api.model.MeasureSystem
import akio.apps.myrun.data.user.api.model.UserProfile
import akio.apps.myrun.domain.activity.ActivityDateTimeFormatter
import akio.apps.myrun.domain.user.GetUserProfileUsecase
import akio.apps.myrun.domain.user.PlaceNameSelector
import akio.apps.myrun.feature.core.Event
import akio.apps.myrun.feature.core.launchcatching.LaunchCatchingDelegate
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import javax.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber

internal class ActivityFeedViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val activityPagingSourceFactory: ActivityPagingSourceFactory,
    private val placeNameSelector: PlaceNameSelector,
    private val userRecentPlaceRepository: UserRecentPlaceRepository,
    private val userAuthenticationState: UserAuthenticationState,
    private val activityLocalStorage: ActivityLocalStorage,
    private val launchCatchingViewModel: LaunchCatchingDelegate,
    private val getUserProfileUsecase: GetUserProfileUsecase,
    private val activityDateTimeFormatter: ActivityDateTimeFormatter,
    private val userPreferences: UserPreferences,
    @NamedIoDispatcher
    private val ioDispatcher: CoroutineDispatcher,
) : ViewModel(), LaunchCatchingDelegate by launchCatchingViewModel {

    private var activityPagingSource: ActivityPagingSource? = null

    private val isUploadBadgeDismissedFlow: MutableStateFlow<Boolean> =
        MutableStateFlow(savedStateHandle.isUploadBadgeDismissed())

    val activityUploadBadge: Flow<ActivityUploadBadgeStatus> =
        createActivityUploadBadgeStatusFlow()

    val userProfile: Flow<UserProfile> =
        getUserProfileUsecase.getUserProfileFlow().mapNotNull { it.data }.flowOn(ioDispatcher)

    val preferredSystem: Flow<MeasureSystem> = userPreferences.getMeasureSystem()

    private fun createActivityUploadBadgeStatusFlow(): Flow<ActivityUploadBadgeStatus> =
        activityLocalStorage.getActivityStorageDataCountFlow()
            .distinctUntilChanged()
            .onEach {
                if (it > 0) {
                    setUploadBadgeDismissed(false)
                }
            }
            .combine(isUploadBadgeDismissedFlow) { localActivityCount, isUploadBadgeDismissed ->
                when {
                    localActivityCount > 0 ->
                        ActivityUploadBadgeStatus.InProgress(localActivityCount)
                    localActivityCount == 0 && !isUploadBadgeDismissed ->
                        ActivityUploadBadgeStatus.Complete
                    else -> ActivityUploadBadgeStatus.Hidden
                }
            }
            // convert to shared flow for multiple collectors
            .shareIn(viewModelScope, replay = 1, started = SharingStarted.WhileSubscribed())

    val myActivityList: Flow<PagingData<BaseActivityModel>> = Pager(
        config = PagingConfig(
            pageSize = PAGE_SIZE,
            enablePlaceholders = false,
            prefetchDistance = PAGE_SIZE
        )
        // do not pass initial key as timestamp because initialKey is reused in invalidating!
        // initialKey = System.currentTimeMillis()
    ) { recreateActivityPagingSource() }
        .flow
        // cachedIn will help latest data to be emitted right after transition
        .cachedIn(viewModelScope)

    private val mapActivityIdToPlaceName: MutableMap<String, String> = mutableMapOf()

    // This is optional data, null for not found.
    private var userRecentPlaceIdentifier: PlaceIdentifier? = null

    private val isInitialLoadingMutable: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val isInitialLoading: Flow<Boolean> = isInitialLoadingMutable

    init {
        loadInitialData()
        observeActivityUploadCount()
    }

    fun setUploadBadgeDismissed(isDismissed: Boolean) {
        isUploadBadgeDismissedFlow.value = isDismissed
        savedStateHandle.setUploadBadgeDismissed(isDismissed)
    }

    fun getFormattedStartTime(activity: BaseActivityModel): ActivityDateTimeFormatter.Result =
        activityDateTimeFormatter.formatActivityDateTime(activity.startTime)

    fun getActivityDisplayPlaceName(activity: BaseActivityModel): String {
        val activityPlaceIdentifier = activity.placeIdentifier
            ?: return ""
        val activityId = activity.id
        var placeName = mapActivityIdToPlaceName[activityId]
        if (placeName != null) {
            return placeName
        }

        placeName = placeNameSelector.select(activityPlaceIdentifier, userRecentPlaceIdentifier)
            ?: ""
        mapActivityIdToPlaceName[activityId] = placeName

        return placeName
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

    private fun recreateActivityPagingSource(): ActivityPagingSource =
        activityPagingSourceFactory.createPagingSource().also {
            Timber.d("recreateActivityPagingSource")
            activityPagingSource = it
        }

    private fun loadInitialData() =
        viewModelScope.launchCatching(
            progressStateFlow = isInitialLoadingMutable,
            errorStateFlow = MutableStateFlow(Event(null))
        ) {
            val userId = userAuthenticationState.getUserAccountId()
            if (userId != null) {
                userRecentPlaceIdentifier = withContext(ioDispatcher) {
                    userRecentPlaceRepository.getRecentPlaceIdentifier(userId)
                }
            }
        }

    private fun SavedStateHandle.isUploadBadgeDismissed(): Boolean =
        this[STATE_IS_UPLOAD_BADGE_DISMISSED] ?: true

    private fun SavedStateHandle.setUploadBadgeDismissed(isDismissed: Boolean) {
        this[STATE_IS_UPLOAD_BADGE_DISMISSED] = isDismissed
    }

    sealed class ActivityUploadBadgeStatus {
        class InProgress(val activityCount: Int) : ActivityUploadBadgeStatus()
        object Complete : ActivityUploadBadgeStatus()
        object Hidden : ActivityUploadBadgeStatus()
    }

    companion object {
        const val PAGE_SIZE = 6

        private const val STATE_IS_UPLOAD_BADGE_DISMISSED = "STATE_IS_UPLOAD_BADGE_DISMISSED"
    }
}
