package akio.apps.myrun.domain.user

import akio.apps.myrun.data.authentication.api.UserAuthenticationState
import akio.apps.myrun.data.user.api.UserFollowRepository
import javax.inject.Inject

class GetUserStatsTypeUsecase @Inject constructor(
    private val userFollowRepository: UserFollowRepository,
    private val userAuthenticationState: UserAuthenticationState,
) {
    suspend fun getUserStatsType(userId: String): UserStatsType {
        val currentUserId = userAuthenticationState.requireUserAccountId()
        if (userId == currentUserId) {
            return UserStatsType.CurrentUser
        }

        return userFollowRepository.getIsFollowing(currentUserId, userId)
            .let {
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
