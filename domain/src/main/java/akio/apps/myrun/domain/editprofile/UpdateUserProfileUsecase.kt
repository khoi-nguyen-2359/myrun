package akio.apps.myrun.domain.editprofile

import akio.apps._base.error.UnauthorizedUserError
import akio.apps.myrun.data.authentication.UserAuthenticationState
import akio.apps.myrun.data.userprofile.UserProfileRepository
import akio.apps.myrun.data.userprofile.model.ProfileEditData
import javax.inject.Inject

class UpdateUserProfileUsecase @Inject constructor(
    private val userProfileRepository: UserProfileRepository,
    private val userAuthenticationState: UserAuthenticationState
) {

    suspend fun updateUserProfile(profileEditData: ProfileEditData) =
        userAuthenticationState.getUserAccountId()
            ?.let { userId ->
                userProfileRepository.updateUserProfile(userId, profileEditData)
            }
            ?: throw UnauthorizedUserError()
}
