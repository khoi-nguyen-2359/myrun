package akio.apps.myrun.domain.user

import akio.apps.myrun.data.authentication.api.UserAuthenticationState
import akio.apps.myrun.data.user.api.UserFollowRepository
import akio.apps.myrun.data.user.api.UserRecentActivityRepository
import akio.apps.myrun.data.user.api.model.FollowStatus
import akio.apps.myrun.data.user.api.model.UserFollow
import akio.apps.myrun.data.user.api.model.UserFollowSuggestion
import akio.apps.myrun.domain.time.TimeProvider
import javax.inject.Inject

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
        val followingUsers = userFollowRepository.getUserFollowings(userId)
        val (requestedList, acceptedList) = categorizeUserIdByFollowStatus(followingUsers)
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
            followSuggestions = followSuggestions.mapNotNull { suggestion ->
                if (suggestion.uid in acceptedList) {
                    null
                } else {
                    suggestion.copy(isRequested = suggestion.uid in requestedList)
                }
            }
        } while (followSuggestions.isEmpty())
        return followSuggestions
    }

    private fun categorizeUserIdByFollowStatus(
        userFollows: List<UserFollow>,
    ): Pair<Set<String>, Set<String>> {
        val requestedList = mutableSetOf<String>()
        val acceptedList = mutableSetOf<String>()
        userFollows.forEach {
            when (it.status) {
                FollowStatus.Requested -> requestedList.add(it.uid)
                FollowStatus.Accepted -> acceptedList.add(it.uid)
            }
        }
        return requestedList to acceptedList
    }

    companion object {
        private const val MAX_NUM = 10L
    }
}
