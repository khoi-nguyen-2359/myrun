package akio.apps.myrun.domain.user

import akio.apps.common.data.Resource
import akio.apps.myrun.data.authentication.UserAuthenticationState
import akio.apps.myrun.data.user.api.UserProfileRepository
import akio.apps.myrun.data.user.api.model.UserProfile
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class GetUserProfileUsecase @Inject constructor(
    private val userProfileRepository: UserProfileRepository,
    private val userAuthenticationState: UserAuthenticationState
) {

    /**
     * [userId] is id of user to fetch data, null will load current user.
     */
    fun getUserProfileFlow(userId: String? = null): Flow<Resource<UserProfile>> = try {
        val finalUserId = userId ?: userAuthenticationState.requireUserAccountId()
        userProfileRepository.getUserProfileFlow(finalUserId)
    } catch (ex: Exception) {
        flowOf(Resource.Error(ex))
    }
}
