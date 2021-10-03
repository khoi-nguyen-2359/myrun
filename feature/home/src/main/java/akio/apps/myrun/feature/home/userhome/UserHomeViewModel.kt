package akio.apps.myrun.feature.home.userhome

import akio.apps.myrun.data.activity.api.ActivityLocalStorage
import akio.apps.myrun.data.activity.api.model.ActivityType
import akio.apps.myrun.data.user.api.PlaceIdentifier
import akio.apps.myrun.data.user.api.model.UserProfile
import akio.apps.myrun.domain.user.impl.GetTrainingSummaryDataUsecase
import akio.apps.myrun.domain.user.impl.GetUserRecentPlaceNameUsecase
import akio.apps.myrun.domain.user.impl.GetUserProfileUsecase
import android.os.Parcelable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.parcelize.Parcelize

class UserHomeViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val getUserProfileUsecase: GetUserProfileUsecase,
    private val getUserRecentPlaceNameUsecase: GetUserRecentPlaceNameUsecase,
    private val getTrainingSummaryDataUsecase: GetTrainingSummaryDataUsecase,
    private val activityLocalStorage: ActivityLocalStorage
) : ViewModel() {

    val screenState: Flow<ScreenState> = combine(
        getUserProfileUsecase.getUserProfileFlow().mapNotNull { it.data },
        getUserRecentPlaceNameUsecase.getUserRecentPlaceNameFlow(),
        getTrainingSummaryDataFlow(),
        ::combineUserProfileAndRecentPlace
    )
        .onStart { emit(savedStateHandle.getScreenState()) }
        .onEach { savedStateHandle.setScreenState(it) }

    private fun getTrainingSummaryDataFlow():
        Flow<Map<ActivityType, GetTrainingSummaryDataUsecase.TrainingSummaryTableData>> =
        activityLocalStorage.getActivityStorageDataCountFlow()
            .distinctUntilChanged()
            .map { getTrainingSummaryDataUsecase.getUserTrainingSummaryData() }

    private fun combineUserProfileAndRecentPlace(
        userProfile: UserProfile,
        userRecentPlace: PlaceIdentifier?,
        trainingSummaryTableData:
            Map<ActivityType, GetTrainingSummaryDataUsecase.TrainingSummaryTableData>,
    ): ScreenState {
        return ScreenState.createScreenState(userRecentPlace, userProfile, trainingSummaryTableData)
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
            val userName: String,
            val userPhotoUrl: String?,
            val userRecentPlace: PlaceIdentifier?,
            val trainingSummaryTableData:
                Map<ActivityType, GetTrainingSummaryDataUsecase.TrainingSummaryTableData>,
        ) : ScreenState(), Parcelable

        companion object {
            fun createScreenState(
                userRecentPlace: PlaceIdentifier?,
                userProfile: UserProfile,
                trainingSummaryTableData:
                    Map<ActivityType, GetTrainingSummaryDataUsecase.TrainingSummaryTableData>,
            ): StatsAvailable {
                return StatsAvailable(
                    userProfile.name,
                    userProfile.photo,
                    userRecentPlace,
                    trainingSummaryTableData
                )
            }
        }
    }

    companion object {
        private const val STATE_SCREEN_STATE = "STATE_SCREEN_STATE"
    }
}
