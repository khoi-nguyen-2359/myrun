package akio.apps.myrun.feature.userhome

import akio.apps.myrun.data.activity.api.model.ActivityModel
import akio.apps.myrun.data.user.api.PlaceIdentifier
import akio.apps.myrun.data.user.api.model.UserProfile
import akio.apps.myrun.domain.recentplace.GetUserRecentPlaceNameUsecase
import akio.apps.myrun.domain.user.GetUserProfileUsecase
import android.os.Parcelable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.parcelize.Parcelize

class UserHomeViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val getUserProfileUsecase: GetUserProfileUsecase,
    private val getUserRecentPlaceNameUsecase: GetUserRecentPlaceNameUsecase,
) : ViewModel() {

    val screenState: Flow<ScreenState> = combine(
        getUserProfileUsecase.getUserProfileFlow().mapNotNull {
            it.data
        },
        getUserRecentPlaceNameUsecase.getUserRecentPlaceNameFlow(),
        ::combineUserProfileAndRecentPlace
    )
        .onStart { emit(savedStateHandle.getScreenState()) }
        .onEach { savedStateHandle.setScreenState(it) }

    private fun combineUserProfileAndRecentPlace(
        userProfile: UserProfile,
        userRecentPlace: PlaceIdentifier?,
    ): ScreenState {
        return ScreenState.createScreenState(userRecentPlace, userProfile, emptyList(), emptyList())
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
            val biWeekSummaryData: List<SummaryData>,
            val biMonthSummaryData: List<SummaryData>
        ) : ScreenState(), Parcelable

        companion object {
            fun createScreenState(
                userRecentPlace: PlaceIdentifier?,
                userProfile: UserProfile,
                biWeekActivities: List<List<ActivityModel>>,
                biMonthActivities: List<List<ActivityModel>>,
            ): StatsAvailable {
                val biWeekSummaryData = biWeekActivities.map { weekActivities ->
                    SummaryData(
                        distance = weekActivities.sumOf { it.distance },
                        duration = weekActivities.sumOf { it.duration }
                    )
                }
                val biMonthSummaryData = biMonthActivities.map { monthActivities ->
                    SummaryData(
                        distance = monthActivities.sumOf { it.distance },
                        duration = monthActivities.sumOf { it.duration }
                    )
                }
                return StatsAvailable(
                    userProfile.name,
                    userProfile.photo,
                    userRecentPlace,
                    biWeekSummaryData,
                    biMonthSummaryData
                )
            }
        }
    }

    @Parcelize
    data class SummaryData(
        val distance: Double,
        val duration: Long,
    ) : Parcelable

    companion object {
        private const val STATE_SCREEN_STATE = "STATE_SCREEN_STATE"
    }
}
