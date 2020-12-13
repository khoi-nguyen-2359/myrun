package akio.apps.myrun.feature.userprofile

import akio.apps._base.data.Resource
import akio.apps._base.error.UnauthorizedUserError
import akio.apps.myrun.data.userprofile.model.UserProfile
import androidx.lifecycle.LiveData
import kotlinx.coroutines.flow.Flow
import kotlin.jvm.Throws

interface GetUserProfileUsecase {
    @Throws(UnauthorizedUserError::class)
    fun getUserProfileFlow(): Flow<Resource<UserProfile>>
}