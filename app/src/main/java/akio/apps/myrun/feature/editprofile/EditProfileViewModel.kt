package akio.apps.myrun.feature.editprofile

import akio.apps._base.lifecycle.Event
import akio.apps._base.viewmodel.BaseViewModel
import akio.apps.myrun.data.userprofile.model.UserProfile
import akio.apps.myrun.data.userprofile.model.ProfileEditData
import androidx.lifecycle.LiveData

abstract class EditProfileViewModel: BaseViewModel(), UserPhoneNumberDelegate {
    abstract val openOtp: LiveData<Event<OtpNavigationInfo>>

    abstract val userProfile: LiveData<UserProfile>

    abstract val stravaTokenExchangedSuccess: LiveData<Event<Unit>>
    abstract val updateProfileSuccess: LiveData<Event<Unit>>

    abstract val blankEditDisplayNameError: LiveData<Event<Unit>>
    abstract val recentLoginRequiredError: LiveData<Event<Unit>>

    abstract fun exchangeStravaToken(stravaLoginCode: String)
    abstract fun updateProfile(profileEditData: ProfileEditData)

    data class OtpNavigationInfo(val phoneNumber: String)
}