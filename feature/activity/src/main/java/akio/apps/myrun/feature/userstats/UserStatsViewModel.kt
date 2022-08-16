package akio.apps.myrun.feature.userstats

import akio.apps.myrun.base.di.NamedIoDispatcher
import akio.apps.myrun.data.activity.api.ActivityLocalStorage
import akio.apps.myrun.data.activity.api.model.ActivityType
import akio.apps.myrun.data.user.api.UserPreferences
import akio.apps.myrun.data.user.api.model.MeasureSystem
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
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.parcelize.Parcelize

internal class UserStatsViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val getUserProfileUsecase: GetUserProfileUsecase,
    private val getUserRecentPlaceNameUsecase: GetUserRecentPlaceNameUsecase,
    private val getTrainingSummaryDataUsecase: GetTrainingSummaryDataUsecase,
    private val activityLocalStorage: ActivityLocalStorage,
    private val userPreferences: UserPreferences,
    @NamedIoDispatcher
    private val ioDispatcher: CoroutineDispatcher,
) : ViewModel() {

    val screenState: Flow<ScreenState> = combine(
        getUserProfileUsecase.getUserProfileFlow().mapNotNull { it.data },
        getUserRecentPlaceNameUsecase.getUserRecentPlaceNameFlow(),
        getTrainingSummaryDataFlow(),
        userPreferences.getMeasureSystem(),
        ::combineScreenStateData
    )
        .onStart { emit(savedStateHandle.getScreenState()) }
        .onEach { savedStateHandle.setScreenState(it) }
        .flowOn(ioDispatcher)

    private fun getTrainingSummaryDataFlow():
        Flow<Map<ActivityType, GetTrainingSummaryDataUsecase.TrainingSummaryTableData>> =
        activityLocalStorage.getActivityStorageDataCountFlow()
            .distinctUntilChanged()
            .map { getTrainingSummaryDataUsecase.getUserTrainingSummaryData() }
            .flowOn(ioDispatcher)

    private fun combineScreenStateData(
        userProfile: UserProfile,
        userRecentPlace: String?,
        trainingSummaryTableData:
            Map<ActivityType, GetTrainingSummaryDataUsecase.TrainingSummaryTableData>,
        measureSystem: MeasureSystem,
    ): ScreenState = ScreenState.createScreenState(
        userRecentPlace,
        userProfile,
        trainingSummaryTableData,
        measureSystem
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
            val userName: String,
            val userPhotoUrl: String?,
            val userRecentPlace: String?,
            val trainingSummaryTableData:
                Map<ActivityType, GetTrainingSummaryDataUsecase.TrainingSummaryTableData>,
            val measureSystem: MeasureSystem,
        ) : ScreenState(), Parcelable

        companion object {
            fun createScreenState(
                userRecentPlace: String?,
                userProfile: UserProfile,
                trainingSummaryTableData:
                    Map<ActivityType, GetTrainingSummaryDataUsecase.TrainingSummaryTableData>,
                measureSystem: MeasureSystem,
            ): StatsAvailable {
                return StatsAvailable(
                    userProfile.name,
                    userProfile.photo,
                    userRecentPlace,
                    trainingSummaryTableData,
                    measureSystem
                )
            }
        }
    }

    companion object {
        private const val STATE_SCREEN_STATE = "STATE_SCREEN_STATE"
    }
}
