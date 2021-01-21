package akio.apps.myrun.feature.userprofile

import akio.apps._base.data.Resource
import akio.apps._base.error.UnauthorizedUserError
import akio.apps.myrun.data.userprofile.model.UserProfile
import kotlinx.coroutines.flow.Flow

interface GetUserProfileUsecase {
    @Throws(UnauthorizedUserError::class)
    fun getUserProfileFlow(): Flow<Resource<UserProfile>>
}
