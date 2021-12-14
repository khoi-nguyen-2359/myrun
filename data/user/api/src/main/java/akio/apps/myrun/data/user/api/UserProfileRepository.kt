package akio.apps.myrun.data.user.api

import akio.apps.myrun.data.Resource
import akio.apps.myrun.data.user.api.model.ProfileEditData
import akio.apps.myrun.data.user.api.model.UserProfile
import android.net.Uri
import kotlinx.coroutines.flow.Flow

interface UserProfileRepository {
    fun getUserProfileFlow(userId: String): Flow<Resource<UserProfile>>
    suspend fun getUserProfile(userId: String): UserProfile

    /**
     * Uploads image file at [imageFileUri] and return the remote resource uri if success, otherwise
     * null.
     */
    suspend fun uploadUserAvatarImage(userId: String, imageFileUri: String): Uri?
    fun updateUserProfile(userId: String, profileEditData: ProfileEditData)
}
