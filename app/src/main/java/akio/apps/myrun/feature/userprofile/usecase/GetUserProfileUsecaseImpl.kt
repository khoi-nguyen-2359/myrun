package akio.apps.myrun.feature.userprofile.usecase

import akio.apps._base.data.Resource
import akio.apps._base.error.UnauthorizedUserError
import akio.apps.myrun.data.authentication.UserAuthenticationState
import akio.apps.myrun.data.userprofile.UserProfileRepository
import akio.apps.myrun.data.userprofile.model.UserProfile
import akio.apps.myrun.feature.userprofile.GetUserProfileUsecase
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import kotlinx.coroutines.ExperimentalCoroutinesApi
import javax.inject.Inject

class GetUserProfileUsecaseImpl @Inject constructor(
    private val userProfileRepository: UserProfileRepository,
    private val userAuthenticationState: UserAuthenticationState
) : GetUserProfileUsecase {
    @ExperimentalCoroutinesApi
    override fun getUserProfile(): LiveData<Resource<UserProfile>> {
        val userId = userAuthenticationState.getUserAccountId()
            ?: throw UnauthorizedUserError()

        return userProfileRepository.getUserProfileFlow(userId)
            .asLiveData(timeoutInMs = 0)
    }
}