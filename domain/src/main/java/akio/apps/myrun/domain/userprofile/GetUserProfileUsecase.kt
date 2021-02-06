package akio.apps.myrun.domain.userprofile

import akio.apps._base.Resource
import akio.apps._base.error.UnauthorizedUserError
import akio.apps.myrun.data.authentication.UserAuthenticationState
import akio.apps.myrun.data.userprofile.UserProfileRepository
import akio.apps.myrun.data.userprofile.model.UserProfile
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetUserProfileUsecase @Inject constructor(
    private val userProfileRepository: UserProfileRepository,
    private val userAuthenticationState: UserAuthenticationState
) {
    @ExperimentalCoroutinesApi
    fun getUserProfileFlow(): Flow<Resource<UserProfile>> {
        val userId = userAuthenticationState.getUserAccountId()
            ?: throw UnauthorizedUserError()

        return userProfileRepository.getUserProfileFlow(userId)
    }
}
