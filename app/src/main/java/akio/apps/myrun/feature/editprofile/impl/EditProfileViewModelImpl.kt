package akio.apps.myrun.feature.editprofile.impl

import akio.apps._base.data.Resource
import akio.apps._base.lifecycle.Event
import akio.apps.myrun.data.userprofile.model.ProfileEditData
import akio.apps.myrun.data.userprofile.model.UserProfile
import akio.apps.myrun.feature.editprofile.EditProfileViewModel
import akio.apps.myrun.feature.editprofile.UpdateUserProfileUsecase
import akio.apps.myrun.feature.editprofile.UserPhoneNumberDelegate
import akio.apps.myrun.feature.userprofile.GetUserProfileUsecase
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.asLiveData
import javax.inject.Inject

class EditProfileViewModelImpl @Inject constructor(
    private val getUserProfileUsecase: GetUserProfileUsecase,
    private val updateUserProfileUsecase: UpdateUserProfileUsecase,
    private val updateUserPhoneDelegate: UserPhoneNumberDelegate
) : EditProfileViewModel(), UserPhoneNumberDelegate by updateUserPhoneDelegate {

    private val _blankEditDisplayNameError = MutableLiveData<Event<Unit>>()
    override val blankEditDisplayNameError: LiveData<Event<Unit>> = _blankEditDisplayNameError

    private val _updateProfileSuccess = MutableLiveData<Event<Unit>>()
    override val updateProfileSuccess: LiveData<Event<Unit>> = _updateProfileSuccess

    private val _stravaTokenExchangedSuccess = MutableLiveData<Event<Unit>>()
    override val stravaTokenExchangedSuccess: LiveData<Event<Unit>> = _stravaTokenExchangedSuccess

    private val _userProfile = MutableLiveData<UserProfile>()
    override val userProfile: LiveData<UserProfile> = _userProfile

    private val _openOpt = MutableLiveData<Event<OtpNavigationInfo>>()
    override val openOtp: LiveData<Event<OtpNavigationInfo>> = _openOpt

    private val liveUserProfile = getUserProfileUsecase.getUserProfileFlow().asLiveData(timeoutInMs = 0)
    private val userProfileObserver = Observer<Resource<UserProfile>> { resource ->
        when (resource) {
            is Resource.Success -> _userProfile.value = resource.data
            is Resource.Error -> _error.value = Event(resource.exception)
        }
    }

    init {
        liveUserProfile.observeForever(userProfileObserver)
    }

    override fun onCleared() {
        super.onCleared()
        liveUserProfile.removeObserver(userProfileObserver)
    }

    private fun getCurrentPhoneNumber() = liveUserProfile.value
        ?.data
        ?.phone

    override fun updateProfile(profileEditData: ProfileEditData) {
        launchCatching {
            if (profileEditData.displayName.isBlank()) {
                _blankEditDisplayNameError.value = Event(Unit)
                return@launchCatching
            }

            updateUserProfileUsecase.updateUserProfile(profileEditData)
            val updatePhone = profileEditData.phoneNumber
            if (updatePhone?.isNotEmpty() == true && updatePhone != getCurrentPhoneNumber()) {
                _openOpt.value = Event(OtpNavigationInfo(updatePhone))
            } else {
                _updateProfileSuccess.value = Event(Unit)
            }
        }
    }
}