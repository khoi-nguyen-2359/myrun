package akio.apps.myrun.feature.userstats

import akio.apps.myrun.base.di.NamedIoDispatcher
import akio.apps.myrun.data.activity.api.ActivityLocalStorage
import akio.apps.myrun.data.activity.api.model.ActivityType
import akio.apps.myrun.data.authentication.api.UserAuthenticationState
import akio.apps.myrun.data.user.api.UserFollowRepository
import akio.apps.myrun.data.user.api.UserPreferences
import akio.apps.myrun.data.user.api.model.MeasureSystem
import akio.apps.myrun.data.user.api.model.UserFollowCounter
import akio.apps.myrun.data.user.api.model.UserProfile
import akio.apps.myrun.domain.user.GetTrainingSummaryDataUsecase
import akio.apps.myrun.domain.user.GetUserProfileUsecase
import akio.apps.myrun.domain.user.GetUserRecentPlaceNameUsecase
import android.os.Parcelable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import javax.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import kotlinx.parcelize.Parcelize

internal class UserStatsViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val getUserProfileUsecase: GetUserProfileUsecase,
    private val getUserRecentPlaceNameUsecase: GetUserRecentPlaceNameUsecase,
    private val getTrainingSummaryDataUsecase: GetTrainingSummaryDataUsecase,
    private val activityLocalStorage: ActivityLocalStorage,
    private val userPreferences: UserPreferences,
    private val authenticationState: UserAuthenticationState,
    private val userFollowRepository: UserFollowRepository,
    @NamedIoDispatcher
    private val ioDispatcher: CoroutineDispatcher,
) : ViewModel() {

    private val userFollowCounterFlow = MutableStateFlow(UserFollowCounter(0, 0))

    val screenState: Flow<ScreenState> = combine(
        getUserProfileUsecase.getUserProfileFlow().mapNotNull { it.data },
        getUserRecentPlaceNameUsecase.getUserRecentPlaceNameFlow(),
        getTrainingSummaryDataFlow(),
        userPreferences.getMeasureSystem(),
        userFollowCounterFlow,
        ::combineScreenStateData
    )
        .onStart { emit(savedStateHandle.getScreenState()) }
        .onEach { savedStateHandle.setScreenState(it) }
        .flowOn(ioDispatcher)

    init {
        getUserFollowCounters()
    }

    private fun getUserFollowCounters() = viewModelScope.launch {
        userFollowCounterFlow.value = userFollowRepository.getUserFollowCounter(
            authenticationState.requireUserAccountId()
        )
    }

    private fun getTrainingSummaryDataFlow():
        Flow<Map<ActivityType, GetTrainingSummaryDataUsecase.TrainingSummaryTableData>> =
        activityLocalStorage.getActivityStorageDataCountFlow()
            .distinctUntilChanged()
            .map { getTrainingSummaryDataUsecase.getUserTrainingSummaryData() }
            .flowOn(ioDispatcher)

    private suspend fun combineScreenStateData(
        userProfile: UserProfile,
        userRecentPlace: String?,
        trainingSummaryTableData:
            Map<ActivityType, GetTrainingSummaryDataUsecase.TrainingSummaryTableData>,
        measureSystem: MeasureSystem,
        userFollowCounter: UserFollowCounter
    ): ScreenState = ScreenState.createScreenState(
        userRecentPlace,
        userProfile,
        trainingSummaryTableData,
        measureSystem,
        userFollowCounter
    )

    private fun SavedStateHandle.getScreenState(): ScreenState =
        this[STATE_SCREEN_STATE] ?: ScreenState.StatsLoading

    private fun SavedStateHandle.setScreenState(screenState: ScreenState) =
        set(STATE_SCREEN_STATE, screenState)

    private fun SavedStateHandle.getUserId(): String = this[STATE_USER_ID] ?: ""

    sealed class ScreenState {
        /**
         * State when user's profile and stats are loading and there's nothing to show.
         */
        @Parcelize
        object StatsLoading : ScreenState(), Parcelable

        @Parcelize
        data class StatsAvailable(
            val userProfile: UserProfile,
            val userRecentPlace: String?,
            val trainingSummaryTableData:
                Map<ActivityType, GetTrainingSummaryDataUsecase.TrainingSummaryTableData>,
            val measureSystem: MeasureSystem,
            val userFollowCounter: UserFollowCounter
        ) : ScreenState(), Parcelable

        companion object {
            fun createScreenState(
                userRecentPlace: String?,
                userProfile: UserProfile,
                trainingSummaryTableData:
                    Map<ActivityType, GetTrainingSummaryDataUsecase.TrainingSummaryTableData>,
                measureSystem: MeasureSystem,
                userFollowCounter: UserFollowCounter
            ): StatsAvailable {
                return StatsAvailable(
                    userProfile,
                    userRecentPlace,
                    trainingSummaryTableData,
                    measureSystem,
                    userFollowCounter
                )
            }
        }
    }

    companion object {
        private const val STATE_SCREEN_STATE = "STATE_SCREEN_STATE"
        private const val STATE_USER_ID = "STATE_USER_ID"

        fun initSavedState(savedStateHandle: SavedStateHandle, userId: String): SavedStateHandle {
            savedStateHandle[STATE_USER_ID] = userId
            return savedStateHandle
        }
    }
}
