package akio.apps.myrun.feature.userprofile.usecase

import akio.apps.myrun.data._base.Resource
import akio.apps._base.error.UnauthorizedUserError
import akio.apps.myrun.data.authentication.UserAuthenticationState
import akio.apps.myrun.data.userprofile.UserProfileRepository
import akio.apps.myrun.data.userprofile.model.UserProfile
import akio.apps.myrun.feature.userprofile.GetUserProfileUsecase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetUserProfileUsecaseImpl @Inject constructor(
    private val userProfileRepository: UserProfileRepository,
    private val userAuthenticationState: UserAuthenticationState
) : GetUserProfileUsecase {
    @ExperimentalCoroutinesApi
    override fun getUserProfileFlow(): Flow<Resource<UserProfile>> {
        val userId = userAuthenticationState.getUserAccountId()
            ?: throw UnauthorizedUserError()

        return userProfileRepository.getUserProfileFlow(userId)
    }
}
