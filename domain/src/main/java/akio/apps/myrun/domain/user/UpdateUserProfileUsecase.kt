package akio.apps.myrun.domain.user

import akio.apps.myrun.data.authentication.UserAuthenticationState
import akio.apps.myrun.data.user.api.UserProfileRepository
import akio.apps.myrun.data.user.api.model.ProfileEditData
import javax.inject.Inject

class UpdateUserProfileUsecase @Inject constructor(
    private val userProfileRepository: UserProfileRepository,
    private val userAuthenticationState: UserAuthenticationState
) {

    suspend fun updateUserProfile(profileEditData: ProfileEditData) =
        userAuthenticationState.requireUserAccountId().let { userId ->
            userProfileRepository.updateUserProfile(userId, profileEditData)
        }
}
