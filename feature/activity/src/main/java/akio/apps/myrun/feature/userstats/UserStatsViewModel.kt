package akio.apps.myrun.feature.userstats

import akio.apps.myrun.data.activity.api.model.ActivityType
import akio.apps.myrun.data.authentication.api.UserAuthenticationState
import akio.apps.myrun.data.user.api.UserFollowRepository
import akio.apps.myrun.data.user.api.UserPreferences
import akio.apps.myrun.data.user.api.UserProfileRepository
import akio.apps.myrun.data.user.api.model.MeasureSystem
import akio.apps.myrun.data.user.api.model.UserFollowCounter
import akio.apps.myrun.data.user.api.model.UserFollowSuggestion
import akio.apps.myrun.data.user.api.model.UserProfile
import akio.apps.myrun.domain.user.GetTrainingSummaryDataUsecase
import akio.apps.myrun.domain.user.GetUserRecentPlaceNameUsecase
import akio.apps.myrun.domain.user.GetUserStatsTypeUsecase
import akio.apps.myrun.feature.core.BaseViewModel
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.mapNotNull

class UserStatsViewModel @Inject constructor(
    private val arguments: UserStatsArguments,
    private val userFollowRepository: UserFollowRepository,
    private val userAuthState: UserAuthenticationState,
    private val userPreferences: UserPreferences,
    private val getTrainingSummaryDataUsecase: GetTrainingSummaryDataUsecase,
    private val getUserRecentPlaceNameUsecase: GetUserRecentPlaceNameUsecase,
    private val getUserStatsTypeUsecase: GetUserStatsTypeUsecase,
    private val userProfileRepository: UserProfileRepository,
) : BaseViewModel() {
    private val currentUserId = userAuthState.requireUserAccountId()

    val userId: String = arguments.userId ?: currentUserId
    val isCurrentUser: Boolean = userId == currentUserId

    @Composable
    fun getUserFollowCounter(): UserFollowCounter = rememberFlow(
        flow = remember { userFollowRepository.getUserFollowCounterFlow(userId) },
        default = UserFollowCounter(0, 0)
    )

    @Composable
    fun getMeasureSystem(): MeasureSystem = rememberFlow(
        flow = remember { userPreferences.getMeasureSystem() },
        default = MeasureSystem.Default
    )

    @Composable
    fun getTrainingSummaryData():
        Map<ActivityType, GetTrainingSummaryDataUsecase.TrainingSummaryTableData> =
        rememberFlow(
            remember {
                getTrainingSummaryDataUsecase.getUserTrainingSummaryDataFlow(userId)
                    .flowOn(Dispatchers.IO)
            },
            mapOf(
                ActivityType.Running to GetTrainingSummaryDataUsecase.TrainingSummaryTableData(),
                ActivityType.Cycling to GetTrainingSummaryDataUsecase.TrainingSummaryTableData()
            )
        )

    @Composable
    fun getUserRecentPlaceName(): String? = rememberFlow(
        flow = remember { getUserRecentPlaceNameUsecase.getUserRecentPlaceNameFlow(userId) },
        default = "--"
    )

    @Composable
    fun getUserType(): GetUserStatsTypeUsecase.UserStatsType = rememberFlow(
        flow = remember { getUserStatsTypeUsecase.getUserStatsTypeFlow(userId) },
        default = GetUserStatsTypeUsecase.UserStatsType.Invalid
    )

    @Composable
    fun getUserProfile(): UserProfile = rememberFlow(
        flow = remember {
            userProfileRepository.getUserProfileResourceFlow(userId).mapNotNull { it.data }
        },
        default = UserProfile(accountId = "", photo = null, name = "--")
    )

    suspend fun followUser(followUserProfile: UserProfile) =
        userFollowRepository.followUser(
            currentUserId,
            UserFollowSuggestion(
                uid = followUserProfile.accountId,
                displayName = followUserProfile.name,
                photoUrl = followUserProfile.photo,
                lastActiveTime = followUserProfile.lastActiveTime
            )
        )

    suspend fun unfollowUser() = userFollowRepository.unfollowUser(currentUserId, userId)

    data class UserStatsArguments(val userId: String?)
}
