package akio.apps.myrun.domain.user

import akio.apps.myrun.data.authentication.api.UserAuthenticationState
import akio.apps.myrun.data.user.api.UserFollowRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

class GetUserStatsTypeUsecase @Inject constructor(
    private val userFollowRepository: UserFollowRepository,
    private val userAuthenticationState: UserAuthenticationState,
) {
    fun getUserStatsTypeFlow(userId: String): Flow<UserStatsType> {
        val currentUserId = userAuthenticationState.requireUserAccountId()
        if (userId == currentUserId) {
            return flowOf(UserStatsType.CurrentUser)
        }

        return userFollowRepository.getIsFollowingFlow(currentUserId, userId)
            .map {
                when (it) {
                    true -> UserStatsType.FollowedUser
                    false -> UserStatsType.NotFollowedUser
                    null -> UserStatsType.Invalid
                }
            }
    }

    enum class UserStatsType {
        CurrentUser, FollowedUser, NotFollowedUser, Invalid
    }
}
