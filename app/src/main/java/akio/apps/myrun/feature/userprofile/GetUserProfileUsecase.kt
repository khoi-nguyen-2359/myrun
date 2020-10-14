package akio.apps.myrun.feature.userprofile

import akio.apps._base.data.Resource
import akio.apps.myrun.data.userprofile.model.UserProfile
import androidx.lifecycle.LiveData

interface GetUserProfileUsecase {
    fun getUserProfile(): LiveData<Resource<UserProfile>>
}