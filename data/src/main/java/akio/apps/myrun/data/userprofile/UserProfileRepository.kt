package akio.apps.myrun.data.userprofile

import akio.apps.myrun.data._base.Resource
import akio.apps.myrun.data.userprofile.model.ProfileEditData
import akio.apps.myrun.data.userprofile.model.UserProfile
import kotlinx.coroutines.flow.Flow

interface UserProfileRepository {
    fun getUserProfileFlow(userId: String): Flow<Resource<UserProfile>>
    suspend fun getUserProfile(userId: String): UserProfile
    suspend fun updateUserProfile(userId: String, profileEditData: ProfileEditData)
}
