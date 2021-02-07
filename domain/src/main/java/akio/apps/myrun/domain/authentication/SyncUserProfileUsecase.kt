package akio.apps.myrun.domain.authentication

import akio.apps.myrun.data.authentication.UserAuthenticationState
import akio.apps.myrun.data.userprofile.UserProfileRepository
import akio.apps.myrun.data.userprofile.model.ProfileEditData
import javax.inject.Inject

/**
 * In case user doesn't make a profile update in onboarding process (for new user), this usecase will clone info from auth account to user profile.
 */
class SyncUserProfileUsecase @Inject constructor(
    private val userAuthenticationState: UserAuthenticationState,
    private val userProfileRepository: UserProfileRepository
) {
    suspend fun syncUserProfile() {
        val userAccount = userAuthenticationState.getUserAccount()
            ?: return

        val editData = ProfileEditData.createFromUserAccount(userAccount)
        userProfileRepository.updateUserProfile(userAccount.uid, editData)
    }
}
