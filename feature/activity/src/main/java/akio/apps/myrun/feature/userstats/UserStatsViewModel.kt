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
import javax.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.parcelize.Parcelize

internal class UserStatsViewModel @Inject constructor(
    getUserProfileUsecase: GetUserProfileUsecase,
    getUserRecentPlaceNameUsecase: GetUserRecentPlaceNameUsecase,
    userPreferences: UserPreferences,
    authenticationState: UserAuthenticationState,
    userFollowRepository: UserFollowRepository,
    private val savedStateHandle: SavedStateHandle,
    private val getTrainingSummaryDataUsecase: GetTrainingSummaryDataUsecase,
    private val activityLocalStorage: ActivityLocalStorage,
    @NamedIoDispatcher
    private val ioDispatcher: CoroutineDispatcher,
) : ViewModel() {

    private val userId: String =
        savedStateHandle.getUserId() ?: authenticationState.requireUserAccountId()

    private val isCurrentUser: Boolean = userId == authenticationState.requireUserAccountId()

    val screenState: Flow<ScreenState> = combine(
        getUserProfileUsecase.getUserProfileFlow(userId).mapNotNull { it.data },
        getUserRecentPlaceNameUsecase.getUserRecentPlaceNameFlow(userId),
        getTrainingSummaryDataFlow(),
        userPreferences.getMeasureSystem(),
        userFollowRepository.getUserFollowCounterFlow(userId),
        ::combineScreenStateData
    )
        .onStart { emit(savedStateHandle.getScreenState()) }
        .onEach { savedStateHandle.setScreenState(it) }
        .flowOn(ioDispatcher)

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
    ): ScreenState = ScreenState.createScreenState(
        userRecentPlace,
        userProfile,
        isCurrentUser,
        trainingSummaryTableData,
        measureSystem,
        userFollowCounter
    )

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
            val isCurrentUser: Boolean,
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
                isCurrentUser: Boolean,
                trainingSummaryTableData:
                    Map<ActivityType, GetTrainingSummaryDataUsecase.TrainingSummaryTableData>,
                measureSystem: MeasureSystem,
                userFollowCounter: UserFollowCounter,
            ): StatsAvailable {
                return StatsAvailable(
                    userProfile,
                    isCurrentUser,
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

        private fun SavedStateHandle.setUserId(userId: String?) {
            this[STATE_USER_ID] = userId
        }

        private fun SavedStateHandle.getUserId(): String? = this[STATE_USER_ID]

        fun initSavedState(savedStateHandle: SavedStateHandle, userId: String?): SavedStateHandle {
            savedStateHandle.setUserId(userId)
            return savedStateHandle
        }
    }
}
