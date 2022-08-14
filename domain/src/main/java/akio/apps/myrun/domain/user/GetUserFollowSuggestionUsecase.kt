package akio.apps.myrun.domain.user

import akio.apps.myrun.data.authentication.api.UserAuthenticationState
import akio.apps.myrun.data.user.api.UserFollowRepository
import akio.apps.myrun.data.user.api.UserRecentActivityRepository
import akio.apps.myrun.data.user.api.model.UserFollowSuggestion
import akio.apps.myrun.domain.time.TimeProvider
import javax.inject.Inject
import kotlinx.coroutines.flow.first

class GetUserFollowSuggestionUsecase @Inject constructor(
    private val userRecentActivityRepository: UserRecentActivityRepository,
    private val userFollowRepository: UserFollowRepository,
    private val authenticationState: UserAuthenticationState,
    private val timeProvider: TimeProvider,
) {
    suspend fun getFollowSuggestion(): List<UserFollowSuggestion> {
        val userId = authenticationState.requireUserAccountId()
        val recentPlace = userRecentActivityRepository.getRecentPlaceIdentifier(userId)
            ?: return emptyList()
        val placeComponent = recentPlace.addressComponents.firstOrNull()
            ?: return emptyList()
        val userFollowingIds = userFollowRepository.getUserFollowingsFlow(userId).first()
            .map { it.uid }
            .toSet()
        var followSuggestions: List<UserFollowSuggestion>
        var startAfterActiveTime = timeProvider.currentTimeMillis()
        do {
            followSuggestions = userFollowRepository.getUserFollowByRecentActivity(
                userId,
                placeComponent,
                MAX_NUM,
                startAfterActiveTime
            )
            if (followSuggestions.isEmpty()) {
                return emptyList()
            }

            startAfterActiveTime = followSuggestions.last().lastActiveTime
            followSuggestions = followSuggestions.filterNot { it.uid in userFollowingIds }
        } while (followSuggestions.isEmpty())
        return followSuggestions
            .flatMap { listOf(it.copy(uid = it.uid + "1"), it.copy(uid = it.uid + "2")) }
            .flatMap { listOf(it.copy(uid = it.uid + "3"), it.copy(uid = it.uid + "4")) }
            .flatMap { listOf(it.copy(uid = it.uid + "5"), it.copy(uid = it.uid + "6")) }
    }

    companion object {
        private const val MAX_NUM = 10L
    }
}
