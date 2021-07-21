package akio.apps.myrun.domain.user

import akio.apps._base.Resource
import akio.apps.myrun.data.authentication.UserAuthenticationState
import akio.apps.myrun.data.userprofile.UserProfileRepository
import akio.apps.myrun.data.userprofile.model.UserProfile
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

class GetUserProfileUsecase @Inject constructor(
    private val userProfileRepository: UserProfileRepository,
    private val userAuthenticationState: UserAuthenticationState
) {

    /**
     * [userId] is id of user to fetch data, null will load current user.
     */
    fun getUserProfileFlow(userId: String? = null): Flow<Resource<UserProfile>> {
        val finalUserId = userId ?: userAuthenticationState.requireUserAccountId()
        return userProfileRepository.getUserProfileFlow(finalUserId)
    }
}
