package akio.apps.myrun.domain.usertimeline

import akio.apps.myrun.data.activity.ActivityRepository
import akio.apps.myrun.data.authentication.UserAuthenticationState
import javax.inject.Inject

class CheckUserTimelineEmptyUsecase @Inject constructor(
    private val activityRepository: ActivityRepository,
    private val userAuthenticationState: UserAuthenticationState
) {
    suspend fun isUserTimelineEmpty(): Boolean {
        val userId = userAuthenticationState.getUserAccountId() ?: return true
        val lastActivities = activityRepository.getActivitiesByStartTime(
            listOf(userId),
            System.currentTimeMillis(),
            1
        )
        return lastActivities.isEmpty()
    }
}
