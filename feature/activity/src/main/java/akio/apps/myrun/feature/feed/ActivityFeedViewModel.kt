package akio.apps.myrun.feature.feed

import akio.apps.myrun.base.di.NamedIoDispatcher
import akio.apps.myrun.data.activity.api.ActivityLocalStorage
import akio.apps.myrun.data.activity.api.model.BaseActivityModel
import akio.apps.myrun.data.authentication.api.UserAuthenticationState
import akio.apps.myrun.data.user.api.UserFollowRepository
import akio.apps.myrun.data.user.api.UserPreferences
import akio.apps.myrun.data.user.api.UserRecentActivityRepository
import akio.apps.myrun.data.user.api.model.MeasureSystem
import akio.apps.myrun.data.user.api.model.PlaceIdentifier
import akio.apps.myrun.data.user.api.model.UserFollow
import akio.apps.myrun.data.user.api.model.UserFollowSuggestion
import akio.apps.myrun.data.user.api.model.UserProfile
import akio.apps.myrun.domain.activity.ActivityDateTimeFormatter
import akio.apps.myrun.domain.user.FollowUserUsecase
import akio.apps.myrun.domain.user.GetUserFollowSuggestionUsecase
import akio.apps.myrun.domain.user.GetUserProfileUsecase
import akio.apps.myrun.domain.user.PlaceNameSelector
import akio.apps.myrun.feature.core.launchcatching.LaunchCatchingDelegate
import akio.apps.myrun.feature.feed.model.FeedActivity
import akio.apps.myrun.feature.feed.model.FeedSuggestedUserFollow
import akio.apps.myrun.feature.feed.model.FeedUiModel
import akio.apps.myrun.feature.feed.model.FeedUserFollowSuggestionList
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.flatMap
import javax.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber

internal class ActivityFeedViewModel @Inject constructor(
    private val activityPagingSourceFactory: ActivityPagingSourceFactory,
    private val placeNameSelector: PlaceNameSelector,
    private val userRecentActivityRepository: UserRecentActivityRepository,
    private val userAuthenticationState: UserAuthenticationState,
    private val activityLocalStorage: ActivityLocalStorage,
    private val launchCatchingViewModel: LaunchCatchingDelegate,
    private val getUserProfileUsecase: GetUserProfileUsecase,
    private val activityDateTimeFormatter: ActivityDateTimeFormatter,
    private val userPreferences: UserPreferences,
    private val getUserFollowUsecase: GetUserFollowSuggestionUsecase,
    private val followUserUsecase: FollowUserUsecase,
    private val userFollowRepository: UserFollowRepository,
    @NamedIoDispatcher
    private val ioDispatcher: CoroutineDispatcher,
) : ViewModel(), LaunchCatchingDelegate by launchCatchingViewModel {

    private val userId: String = userAuthenticationState.requireUserAccountId()

    private var activityPagingSource: ActivityPagingSource? = null

    val userProfile: Flow<UserProfile> =
        getUserProfileUsecase.getUserProfileFlow(userId).mapNotNull { it.data }.flowOn(ioDispatcher)

    val preferredSystem: Flow<MeasureSystem> = userPreferences.getMeasureSystem()

    private var cachedUserFollowSuggestions: List<UserFollowSuggestion>? = null

    private val userFollowSuggestionMutableFlow: MutableSharedFlow<List<UserFollowSuggestion>> =
        MutableSharedFlow()
    private val userFollowActionStateMap: MutableMap<String, Boolean> = mutableMapOf()

    val myActivityList: Flow<PagingData<FeedUiModel>> = Pager(
        config = PagingConfig(
            pageSize = PAGE_SIZE,
            enablePlaceholders = false,
            prefetchDistance = PAGE_SIZE
        )
        // do not pass initial key as timestamp because initialKey is reused in invalidating!
        // initialKey = System.currentTimeMillis()
    ) { recreateActivityPagingSource() }
        .flow
        .cachedIn(viewModelScope)
        .combine(userFollowSuggestionMutableFlow) { pagingData, userFollows ->
            combinePagingDataSources(pagingData, userFollows)
        }
        // cachedIn will help latest data to be emitted right after transition
        .cachedIn(viewModelScope)

    private fun combinePagingDataSources(
        pagingData: PagingData<BaseActivityModel>,
        userFollows: List<UserFollowSuggestion>,
    ): PagingData<FeedUiModel> {
        var index = 0
        return pagingData.flatMap {
            if (index++ == PAGE_SIZE / 2 && userFollows.isNotEmpty()) {
                // insert follow suggestion in the end of the first page
                val feedModels = userFollows.map { dataModel ->
                    FeedSuggestedUserFollow(
                        dataModel,
                        userFollowActionStateMap[dataModel.uid] ?: false
                    )
                }
                listOf(
                    FeedActivity(it, it.athleteInfo.userId == userId),
                    FeedUserFollowSuggestionList(feedModels)
                )
            } else {
                listOf(FeedActivity(it, it.athleteInfo.userId == userId))
            }
        }
    }

    private val mapActivityIdToPlaceName: MutableMap<String, String> = mutableMapOf()

    // This is optional data, null for not found.
    private var userRecentPlaceIdentifier: PlaceIdentifier? = null

    private val isInitialLoadingMutable: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val isInitialLoading: Flow<Boolean> = isInitialLoadingMutable

    private val userFollowingsFlow: Flow<List<UserFollow>> =
        userFollowRepository.getUserFollowingsFlow(userId)

    init {
        loadInitialData()
        observeActivityUploadCount()
        loadUserFollowSuggestions()
        observeUserFollowings()
    }

    private fun observeUserFollowings() = viewModelScope.launch {
        userFollowingsFlow.collect { userFollowings ->
            userFollowActionStateMap.clear()
            userFollowings.forEach { userFollow ->
                userFollowActionStateMap[userFollow.uid] = true
            }
            emitCachedUserFollowSuggestions()
        }
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

    fun followUser(userFollowSuggestion: UserFollowSuggestion) = viewModelScope.launch {
        try {
            userFollowActionStateMap[userFollowSuggestion.uid] = true
            emitCachedUserFollowSuggestions()
            withContext(ioDispatcher) {
                followUserUsecase.followUser(userFollowSuggestion)
            }
        } catch (ex: Exception) {
            setLaunchCatchingError(ex)
            userFollowActionStateMap[userFollowSuggestion.uid] = false
        }
        emitCachedUserFollowSuggestions()
    }

    private fun loadUserFollowSuggestions() = viewModelScope.launch {
        cachedUserFollowSuggestions = withContext(ioDispatcher) {
            getUserFollowUsecase.getFollowSuggestion()
        }

        emitCachedUserFollowSuggestions()
    }

    private suspend fun emitCachedUserFollowSuggestions() {
        cachedUserFollowSuggestions?.let { suggestions ->
            userFollowSuggestionMutableFlow.emit(suggestions)
        }
    }

    private fun observeActivityUploadCount() = viewModelScope.launch {
        activityLocalStorage.getActivityStorageDataCountFlow()
            .distinctUntilChanged()
            .collect {
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
            loadingStateFlow = isInitialLoadingMutable,
            errorStateFlow = MutableStateFlow(null)
        ) {
            val userId = userAuthenticationState.getUserAccountId()
            if (userId != null) {
                userRecentPlaceIdentifier = withContext(ioDispatcher) {
                    userRecentActivityRepository.getRecentPlaceIdentifier(userId)
                }
            }
        }

    companion object {
        const val PAGE_SIZE = 6
    }
}
