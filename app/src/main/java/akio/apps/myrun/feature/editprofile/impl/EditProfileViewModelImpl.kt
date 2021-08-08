package akio.apps.myrun.feature.editprofile.impl

import akio.apps._base.Resource
import akio.apps.base.feature.lifecycle.Event
import akio.apps.myrun.data.userprofile.model.ProfileEditData
import akio.apps.myrun.data.userprofile.model.UserProfile
import akio.apps.myrun.domain.user.GetUserProfileUsecase
import akio.apps.myrun.domain.user.UpdateUserProfileUsecase
import akio.apps.myrun.feature.editprofile.EditProfileViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.asLiveData
import javax.inject.Inject

class EditProfileViewModelImpl @Inject constructor(
    getUserProfileUsecase: GetUserProfileUsecase,
    private val updateUserProfileUsecase: UpdateUserProfileUsecase
) : EditProfileViewModel() {

    private val _blankEditDisplayNameError = MutableLiveData<Event<Unit>>()
    override val blankEditDisplayNameError: LiveData<Event<Unit>> = _blankEditDisplayNameError

    private val _updateProfileSuccess = MutableLiveData<Event<Unit>>()
    override val updateProfileSuccess: LiveData<Event<Unit>> = _updateProfileSuccess

    private val _userProfile = MutableLiveData<UserProfile>()
    override val userProfile: LiveData<UserProfile> = _userProfile

    private val liveUserProfile = getUserProfileUsecase.getUserProfileFlow()
        .asLiveData(timeoutInMs = 0)
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

    override fun updateProfile(profileEditData: ProfileEditData) {
        launchCatching {
            if (profileEditData.displayName.isBlank()) {
                _blankEditDisplayNameError.value = Event(Unit)
                return@launchCatching
            }

            updateUserProfileUsecase.updateUserProfile(profileEditData)
            _updateProfileSuccess.value = Event(Unit)
        }
    }
}
