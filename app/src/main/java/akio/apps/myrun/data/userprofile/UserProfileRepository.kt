package akio.apps.myrun.data.userprofile

import akio.apps._base.data.Resource
import akio.apps.myrun.data.userprofile.model.UserProfile
import akio.apps.myrun.feature.userprofile.impl.ProfileEditData
import kotlinx.coroutines.flow.Flow

interface UserProfileRepository {
    fun getUserProfileFlow(userId: String): Flow<Resource<UserProfile>>
    suspend fun getUserProfile(userId: String): UserProfile?

    /**
     * Exception:
     * + LoginSessionExpiredError
     * + UnauthorizedUserError
     */
    suspend fun updateUserProfile(userId: String, profileEditData: ProfileEditData)
}
