package akio.apps.myrun.feature.editprofile

import akio.apps._base.data.Resource
import akio.apps.myrun.feature.userprofile.impl.ProfileEditData

interface UpdateUserProfileUsecase {

    suspend fun updateUserProfile(profileEditData: ProfileEditData): Resource<Unit>
}