package akio.apps.myrun.feature.editprofile.impl

import akio.apps._base.data.Resource
import akio.apps._base.error.UnauthorizedUserError
import akio.apps.myrun.data.authentication.UserAuthenticationState
import akio.apps.myrun.data.userprofile.UserProfileRepository
import akio.apps.myrun.feature.editprofile.UpdateUserProfileUsecase
import akio.apps.myrun.feature.userprofile.impl.ProfileEditData
import javax.inject.Inject

class UpdateUserProfileUsecaseImpl @Inject constructor(
    private val userProfileRepository: UserProfileRepository,
    private val userAuthenticationState: UserAuthenticationState
) : UpdateUserProfileUsecase {

    override suspend fun updateUserProfile(profileEditData: ProfileEditData): Resource<Unit> = userAuthenticationState.getUserAccountId()?.let { userId ->
        return try {
            userProfileRepository.updateUserProfile(userId, profileEditData)
            Resource.Success(Unit)
        } catch (ex: Exception) {
            Resource.Error(ex)
        }
    }
        ?: throw UnauthorizedUserError()
}