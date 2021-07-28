package akio.apps.myrun.feature.editprofile

import akio.apps._base.lifecycle.Event
import akio.apps._base.viewmodel.BaseViewModel
import akio.apps.myrun.data.userprofile.model.ProfileEditData
import akio.apps.myrun.data.userprofile.model.UserProfile
import androidx.lifecycle.LiveData

abstract class EditProfileViewModel : BaseViewModel() {
    abstract val userProfile: LiveData<UserProfile>

    abstract val updateProfileSuccess: LiveData<Event<Unit>>

    abstract val blankEditDisplayNameError: LiveData<Event<Unit>>

    abstract fun updateProfile(profileEditData: ProfileEditData)
}
