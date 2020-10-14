package akio.apps.myrun.feature.userprofile.usecase

import akio.apps._base.data.Resource
import akio.apps.myrun.data.authentication.UserAuthenticationState
import akio.apps.myrun.data.userprofile.UserProfileRepository
import akio.apps.myrun.data.userprofile.model.UserProfile
import akio.apps.myrun.feature.userprofile.GetUserProfileUsecase
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flatMapLatest
import javax.inject.Inject

class GetUserProfileUsecaseImpl @Inject constructor(
    private val userProfileRepository: UserProfileRepository,
    private val userAuthenticationState: UserAuthenticationState
): GetUserProfileUsecase {
    @ExperimentalCoroutinesApi
    override fun getUserProfile(): LiveData<Resource<UserProfile>> {
        return userAuthenticationState.getUserAccountFlow()
            .flatMapLatest {
                userProfileRepository.getUserProfileFlow(it.uid)
            }
            .asLiveData(timeoutInMs = 0)
    }
}