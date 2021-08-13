package akio.apps.myrun.data.user.api

import akio.apps.common.data.Resource
import akio.apps.myrun.data.user.api.model.ProfileEditData
import akio.apps.myrun.data.user.api.model.UserProfile
import kotlinx.coroutines.flow.Flow

interface UserProfileRepository {
    fun getUserProfileFlow(userId: String): Flow<Resource<UserProfile>>
    suspend fun getUserProfile(userId: String): UserProfile
    suspend fun updateUserProfile(userId: String, profileEditData: ProfileEditData)
}
