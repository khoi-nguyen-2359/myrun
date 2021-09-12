package akio.apps.myrun.feature.userhome

import akio.apps.myrun.data.activity.api.ActivityRepository
import akio.apps.myrun.data.activity.api.model.ActivityModel
import akio.apps.myrun.data.authentication.api.UserAuthenticationState
import akio.apps.myrun.data.user.api.PlaceIdentifier
import akio.apps.myrun.data.user.api.UserProfileRepository
import akio.apps.myrun.data.user.api.UserRecentPlaceRepository
import akio.apps.myrun.data.user.api.model.UserProfile
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class UserHomeViewModel @Inject constructor(
    private val userAuthState: UserAuthenticationState,
    private val userProfileRepository: UserProfileRepository,
    private val userRecentPlaceRepository: UserRecentPlaceRepository,
    private val activityRepository: ActivityRepository,
) : ViewModel() {

    private val mutableScreenState: MutableStateFlow<ScreenState> =
        MutableStateFlow(ScreenState.StatsLoading)
    val screenState: Flow<ScreenState> = mutableScreenState

    init {
        loadInitialData()
    }

    private fun loadInitialData() = viewModelScope.launch {
        val userId = userAuthState.requireUserAccountId()
        val userProfile = userProfileRepository.getUserProfile(userId)
        val userRecentPlace = userRecentPlaceRepository.getRecentPlaceIdentifier(userId)
        mutableScreenState.value =
            ScreenState.createScreenState(userRecentPlace, userProfile, emptyList(), emptyList())
    }

    sealed class ScreenState {
        /**
         * State when user's profile and stats are loading and there's nothing to show.
         */
        object StatsLoading : ScreenState()

        data class StatsAvailable(
            val userName: String,
            val userPhotoUrl: String?,
            val userRecentPlace: PlaceIdentifier?,
            val runSummaries: List<SummaryData>,
            val rideSummaries: List<SummaryData>,
        ) : ScreenState()

        companion object {
            fun createScreenState(
                userRecentPlace: PlaceIdentifier?,
                userProfile: UserProfile,
                runActivityLists: List<List<ActivityModel>>,
                rideActivityLists: List<List<ActivityModel>>,
            ): StatsAvailable {
                val runSummaryData = runActivityLists.map { runsInAPeriod ->
                    SummaryData(
                        distance = runsInAPeriod.sumOf { it.distance },
                        duration = runsInAPeriod.sumOf { it.duration }
                    )
                }
                val rideSummaryData = rideActivityLists.map { ridesInAPeriod ->
                    SummaryData(
                        distance = ridesInAPeriod.sumOf { it.distance },
                        duration = ridesInAPeriod.sumOf { it.duration }
                    )
                }
                return StatsAvailable(
                    userProfile.name,
                    userProfile.photo,
                    userRecentPlace,
                    runSummaryData,
                    rideSummaryData
                )
            }
        }
    }

    data class SummaryData(
        val distance: Double,
        val duration: Long,
    )
}
