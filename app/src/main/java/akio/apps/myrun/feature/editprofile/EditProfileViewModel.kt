package akio.apps.myrun.feature.editprofile

import akio.apps.common.feature.lifecycle.Event
import akio.apps.common.feature.viewmodel.BaseViewModel
import akio.apps.myrun.data.user.api.model.ProfileEditData
import akio.apps.myrun.data.user.api.model.UserProfile
import androidx.lifecycle.LiveData

abstract class EditProfileViewModel : BaseViewModel() {
    abstract val userProfile: LiveData<UserProfile>

    abstract val updateProfileSuccess: LiveData<Event<Unit>>

    abstract val blankEditDisplayNameError: LiveData<Event<Unit>>

    abstract fun updateProfile(profileEditData: ProfileEditData)
}
