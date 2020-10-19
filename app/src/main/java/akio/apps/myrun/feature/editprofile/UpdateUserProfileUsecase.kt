package akio.apps.myrun.feature.editprofile

import akio.apps.myrun.data.userprofile.model.ProfileEditData

interface UpdateUserProfileUsecase {
    suspend fun updateUserProfile(profileEditData: ProfileEditData)
}