package akio.apps.myrun.domain.user

import akio.apps.myrun.data.authentication.api.UserAuthenticationState
import akio.apps.myrun.data.user.api.UserRecentActivityRepository
import akio.apps.myrun.data.user.api.model.UserFollow
import javax.inject.Inject

class GetUserFollowSuggestionUsecase @Inject constructor(
    private val userRecentActivityRepository: UserRecentActivityRepository,
    private val authenticationState: UserAuthenticationState,
) {
    suspend fun getFollowSuggestion(): List<UserFollow> {
        val userId = authenticationState.requireUserAccountId()
        val recentPlace = userRecentActivityRepository.getRecentPlaceIdentifier(userId)
            ?: return emptyList()
        val placeComponent = recentPlace.addressComponents.firstOrNull()
            ?: return emptyList()
        return userRecentActivityRepository.getUserFollowByRecentActivity(
            userId,
            placeComponent,
            MAX_NUM
        )
    }

    companion object {
        private const val MAX_NUM = 10L
    }
}
