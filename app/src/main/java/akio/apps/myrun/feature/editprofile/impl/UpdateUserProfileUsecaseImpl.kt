package akio.apps.myrun.feature.editprofile.impl

import akio.apps._base.error.UnauthorizedUserError
import akio.apps.myrun.data.authentication.UserAuthenticationState
import akio.apps.myrun.data.userprofile.UserProfileRepository
import akio.apps.myrun.data.userprofile.model.ProfileEditData
import akio.apps.myrun.feature.editprofile.UpdateUserProfileUsecase
import javax.inject.Inject

class UpdateUserProfileUsecaseImpl @Inject constructor(
    private val userProfileRepository: UserProfileRepository,
    private val userAuthenticationState: UserAuthenticationState
) : UpdateUserProfileUsecase {

    override suspend fun updateUserProfile(profileEditData: ProfileEditData) =
        userAuthenticationState.getUserAccountId()
            ?.let { userId ->
                userProfileRepository.updateUserProfile(userId, profileEditData)
            }
            ?: throw UnauthorizedUserError()
}
