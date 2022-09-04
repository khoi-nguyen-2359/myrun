package akio.apps.myrun.feature.userstats

import akio.apps.myrun.base.di.NamedIoDispatcher
import akio.apps.myrun.data.activity.api.ActivityLocalStorage
import akio.apps.myrun.data.activity.api.model.ActivityType
import akio.apps.myrun.data.authentication.api.UserAuthenticationState
import akio.apps.myrun.data.user.api.UserFollowRepository
import akio.apps.myrun.data.user.api.UserPreferences
import akio.apps.myrun.data.user.api.model.MeasureSystem
import akio.apps.myrun.data.user.api.model.UserFollowCounter
import akio.apps.myrun.data.user.api.model.UserFollowSuggestion
import akio.apps.myrun.data.user.api.model.UserProfile
import akio.apps.myrun.domain.user.GetTrainingSummaryDataUsecase
import akio.apps.myrun.domain.user.GetUserProfileUsecase
import akio.apps.myrun.domain.user.GetUserRecentPlaceNameUsecase
import akio.apps.myrun.domain.user.GetUserStatsTypeUsecase
import akio.apps.myrun.feature.core.launchcatching.LaunchCatchingDelegate
import android.os.Parcelable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import javax.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.parcelize.Parcelize

internal class UserStatsViewModel @Inject constructor(
    getUserProfileUsecase: GetUserProfileUsecase,
    getUserRecentPlaceNameUsecase: GetUserRecentPlaceNameUsecase,
    userPreferences: UserPreferences,
    authenticationState: UserAuthenticationState,
    getUserStatsTypeUsecase: GetUserStatsTypeUsecase,
    private val userFollowRepository: UserFollowRepository,
    private val savedStateHandle: SavedStateHandle,
    private val getTrainingSummaryDataUsecase: GetTrainingSummaryDataUsecase,
    private val activityLocalStorage: ActivityLocalStorage,
    private val launchCatchingDelegate: LaunchCatchingDelegate,
    @NamedIoDispatcher
    private val ioDispatcher: CoroutineDispatcher,
) : ViewModel(), LaunchCatchingDelegate by launchCatchingDelegate {

    private val currentUserId: String by lazy { authenticationState.requireUserAccountId() }
    private val userId: String by lazy { savedStateHandle.getUserId() ?: currentUserId }
    private val isCurrentUser: Boolean by lazy { userId == currentUserId }

    private val userProfileStateFlow: StateFlow<UserProfile> =
        getUserProfileUsecase.getUserProfileFlow(userId).mapNotNull { it.data }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), makeDummyUserProfile())

    val screenState: Flow<ScreenState> = combine(
        userProfileStateFlow,
        getUserRecentPlaceNameUsecase.getUserRecentPlaceNameFlow(userId),
        getTrainingSummaryDataFlow(),
        userPreferences.getMeasureSystem(),
        userFollowRepository.getUserFollowCounterFlow(userId),
        getUserStatsTypeUsecase.getUserStatsTypeFlow(userId),
        ::combineScreenStateData
    )
        .onStart { emit(savedStateHandle.getScreenState()) }
        .onEach { savedStateHandle.setScreenState(it) }
        .flowOn(ioDispatcher)

    fun followUser() = viewModelScope.launchCatching {
        val followSuggestion = userProfileStateFlow.value.let {
            UserFollowSuggestion(it.accountId, it.name, it.photo, it.lastActiveTime)
        }
        userFollowRepository.followUser(currentUserId, followSuggestion)
    }

    fun unfollowUser() = viewModelScope.launchCatching {
        userFollowRepository.unfollowUser(currentUserId, userId)
    }

    private fun getTrainingSummaryDataFlow():
        Flow<Map<ActivityType, GetTrainingSummaryDataUsecase.TrainingSummaryTableData>> =
        if (isCurrentUser) {
            activityLocalStorage.getActivityStorageDataCountFlow()
                .distinctUntilChanged()
                .map { getTrainingSummaryDataUsecase.getUserTrainingSummaryData(userId) }
        } else {
            flow {
                emit(getTrainingSummaryDataUsecase.getUserTrainingSummaryData(userId))
            }
        }
            .flowOn(ioDispatcher)

    private suspend fun combineScreenStateData(
        userProfile: UserProfile,
        userRecentPlace: String?,
        trainingSummaryTableData:
        Map<ActivityType, GetTrainingSummaryDataUsecase.TrainingSummaryTableData>,
        measureSystem: MeasureSystem,
        userFollowCounter: UserFollowCounter,
        userType: GetUserStatsTypeUsecase.UserStatsType,
    ): ScreenState {
        return ScreenState.createScreenState(
            userRecentPlace,
            userProfile,
            userType,
            trainingSummaryTableData,
            measureSystem,
            userFollowCounter
        )
    }

    private fun SavedStateHandle.getScreenState(): ScreenState =
        this[STATE_SCREEN_STATE] ?: ScreenState.StatsLoading

    private fun SavedStateHandle.setScreenState(screenState: ScreenState) =
        set(STATE_SCREEN_STATE, screenState)

    sealed class ScreenState {
        /**
         * State when user's profile and stats are loading and there's nothing to show.
         */
        @Parcelize
        object StatsLoading : ScreenState(), Parcelable

        @Parcelize
        data class StatsAvailable(
            val userProfile: UserProfile,
            val userType: GetUserStatsTypeUsecase.UserStatsType,
            val userRecentPlace: String?,
            val trainingSummaryTableData:
            Map<ActivityType, GetTrainingSummaryDataUsecase.TrainingSummaryTableData>,
            val measureSystem: MeasureSystem,
            val userFollowCounter: UserFollowCounter,
        ) : ScreenState(), Parcelable

        companion object {
            fun createScreenState(
                userRecentPlace: String?,
                userProfile: UserProfile,
                userType: GetUserStatsTypeUsecase.UserStatsType,
                trainingSummaryTableData:
                Map<ActivityType, GetTrainingSummaryDataUsecase.TrainingSummaryTableData>,
                measureSystem: MeasureSystem,
                userFollowCounter: UserFollowCounter,
            ): StatsAvailable {
                return StatsAvailable(
                    userProfile,
                    userType,
                    userRecentPlace,
                    trainingSummaryTableData,
                    measureSystem,
                    userFollowCounter
                )
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun <T1, T2, T3, T4, T5, T6, R> combine(
        flow: Flow<T1>,
        flow2: Flow<T2>,
        flow3: Flow<T3>,
        flow4: Flow<T4>,
        flow5: Flow<T5>,
        flow6: Flow<T6>,
        transform: suspend (T1, T2, T3, T4, T5, T6) -> R,
    ): Flow<R> = combine(flow, flow2, flow3, flow4, flow5, flow6) { args: Array<*> ->
        transform(
            args[0] as T1,
            args[1] as T2,
            args[2] as T3,
            args[3] as T4,
            args[4] as T5,
            args[5] as T6
        )
    }

    companion object {
        private const val STATE_SCREEN_STATE = "STATE_SCREEN_STATE"
        private const val STATE_USER_ID = "STATE_USER_ID"

        private fun SavedStateHandle.setUserId(userId: String?) {
            this[STATE_USER_ID] = userId
        }

        private fun SavedStateHandle.getUserId(): String? = this[STATE_USER_ID]

        private fun makeDummyUserProfile(): UserProfile = UserProfile(
            accountId = "", photo = null
        )

        fun initSavedState(savedStateHandle: SavedStateHandle, userId: String?): SavedStateHandle {
            savedStateHandle.setUserId(userId)
            return savedStateHandle
        }
    }
}
