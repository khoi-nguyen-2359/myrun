package akio.apps.myrun.feature.signin.impl

import akio.apps.myrun.data.authentication.UserAuthenticationState
import akio.apps.myrun.data.userprofile.UserProfileRepository
import akio.apps.myrun.feature.signin.SyncUserProfileUsecase
import akio.apps.myrun.data.userprofile.model.ProfileEditData
import javax.inject.Inject

class SyncUserProfileUsecaseImpl @Inject constructor(
    private val userAuthenticationState: UserAuthenticationState,
    private val userProfileRepository: UserProfileRepository
) : SyncUserProfileUsecase {
    override suspend fun syncUserProfile() {
        val userAccount = userAuthenticationState.getUserAccount()
            ?: return

        val editData = ProfileEditData.createFromUserAccount(userAccount)
        userProfileRepository.updateUserProfile(userAccount.uid, editData)
    }
}