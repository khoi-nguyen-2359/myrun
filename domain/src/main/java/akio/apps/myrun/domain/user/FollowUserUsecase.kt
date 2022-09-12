package akio.apps.myrun.domain.user

import akio.apps.myrun.data.authentication.api.UserAuthenticationState
import akio.apps.myrun.data.user.api.UserFollowRepository
import akio.apps.myrun.data.user.api.model.UserFollowSuggestion
import javax.inject.Inject

class FollowUserUsecase @Inject constructor(
    private val userFollowRepository: UserFollowRepository,
    private val authState: UserAuthenticationState,
) {
    suspend fun followUser(followSuggestion: UserFollowSuggestion) {
        val currentUserId = authState.requireUserAccountId()
        userFollowRepository.followUser(currentUserId, followSuggestion)
    }
}
