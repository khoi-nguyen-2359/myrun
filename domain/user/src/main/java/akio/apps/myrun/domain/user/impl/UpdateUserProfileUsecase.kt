package akio.apps.myrun.domain.user.impl

import akio.apps.myrun.data.authentication.api.UserAuthenticationState
import akio.apps.myrun.data.user.api.UserProfileRepository
import akio.apps.myrun.data.user.api.model.ProfileEditData
import javax.inject.Inject

class UpdateUserProfileUsecase @Inject constructor(
    private val userProfileRepository: UserProfileRepository,
    private val userAuthenticationState: UserAuthenticationState,
) {

    /**
     * Updates current user profile with given [profileEditData]. This method has no returning
     * signal, just fire and forget.
     */
    fun updateUserProfile(profileEditData: ProfileEditData) =
        userAuthenticationState.requireUserAccountId().let { userId ->
            userProfileRepository.updateUserProfile(userId, profileEditData)
        }
}
