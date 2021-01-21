package akio.apps.myrun.feature.editprofile

import akio.apps._base.error.UnauthorizedUserError
import akio.apps.myrun.data.userprofile.model.ProfileEditData

interface UpdateUserProfileUsecase {
    @Throws(UnauthorizedUserError::class)
    suspend fun updateUserProfile(profileEditData: ProfileEditData)
}
