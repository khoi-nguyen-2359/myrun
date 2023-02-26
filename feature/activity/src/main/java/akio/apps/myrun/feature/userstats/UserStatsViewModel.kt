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
import akio.apps.myrun.feature.userstats.model.UserStatsProfileUiModel
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.mapNotNull
import timber.log.Timber

class UserStatsViewModel @Inject constructor(
    private val arguments: UserStatsArguments,
    private val userFollowRepository: UserFollowRepository,
    private val userAuthState: UserAuthenticationState,
    private val userPreferences: UserPreferences,
    private val getTrainingSummaryDataUsecase: GetTrainingSummaryDataUsecase,
    private val getUserRecentPlaceNameUsecase: GetUserRecentPlaceNameUsecase,
    private val getUserStatsTypeUsecase: GetUserStatsTypeUsecase,
    private val userProfileRepository: UserProfileRepository,
) {
    private val currentUserId = userAuthState.requireUserAccountId()

    val userId: String = arguments.userId ?: currentUserId
    val isCurrentUser: Boolean = userId == currentUserId

    val measureSystemFlow: Flow<MeasureSystem> = userPreferences.getMeasureSystemFlow()

    val trainingSummaryDataFlow:
        Flow<Map<ActivityType, GetTrainingSummaryDataUsecase.TrainingSummaryTableData>> =
        getTrainingSummaryDataUsecase.getUserTrainingSummaryDataFlow(userId).flowOn(Dispatchers.IO)

    private val userRecentPlaceFlow: Flow<String?> = flow {
        emit(getUserRecentPlaceNameUsecase.getUserRecentPlaceName(userId))
    }
        .flowOn(Dispatchers.IO)

    private val userProfileFlow =
        userProfileRepository.getUserProfileResourceFlow(userId)
            .mapNotNull { it.data }
            .flowOn(Dispatchers.IO)

    val userStatsProfileFlow: Flow<UserStatsProfileUiModel> = combine(
        userProfileFlow,
        userRecentPlaceFlow,
        ::UserStatsProfileUiModel
    )

    init {
        Timber.d("init ViewModel")
    }

    val userFollowCounterFlow: Flow<UserFollowCounter> =
        userFollowRepository.getUserFollowCounterFlow(userId)

    val userTypeFlow: Flow<GetUserStatsTypeUsecase.UserStatsType> = flow {
        emit(getUserStatsTypeUsecase.getUserStatsType(userId))
    }

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
