package akio.apps.myrun.feature.userprofile.impl

import akio.apps._base.data.Resource
import akio.apps._base.lifecycle.Event
import akio.apps.myrun.data.authentication.UserAuthenticationState
import akio.apps.myrun.data.externalapp.model.ExternalAppToken
import akio.apps.myrun.data.externalapp.model.ProviderToken
import akio.apps.myrun.data.externalapp.model.RunningApp
import akio.apps.myrun.data.userprofile.model.UserProfile
import akio.apps.myrun.feature.userprofile.*
import android.util.EventLog
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import javax.inject.Inject

class UserProfileViewModelImpl @Inject constructor(
    private val userAuthenticationState: UserAuthenticationState,
    private val linkFacebookUsecase: LinkFacebookUsecase,
    private val getUserProfileUsecase: GetUserProfileUsecase,
    private val getProviderTokensUsecase: GetProviderTokensUsecase,
    private val deauthorizeStravaUsecase: DeauthorizeStravaUsecase,
    private val removeStravaTokenUsecase: RemoveStravaTokenUsecase,
    private val logoutUsecase: LogoutUsecase
): UserProfileViewModel() {

    private val _isInlineLoading = MutableLiveData<Boolean>()
    override val isInlineLoading: LiveData<Boolean> = _isInlineLoading

    private val liveUserProfile = MutableLiveData<UserProfile>()
    override fun getUserProfileAlive(): LiveData<UserProfile> = liveUserProfile

    private val isFacebookAccountLinked = MutableLiveData<Boolean>(userAuthenticationState.isLinkedWithFacebook())
    override fun isFacebookAccountLinked(): LiveData<Boolean> = isFacebookAccountLinked

    private val liveUserProfileResource = getUserProfileUsecase.getUserProfile()

    private val liveProviders = getProviderTokensUsecase.getProviderTokens()
    override fun getProvidersAlive() = liveProviders

    private val userProfileResourceObserver = Observer<Resource<UserProfile>> {
        when (it) {
            is Resource.Error -> _error.value = Event(it.exception)
            is Resource.Success -> liveUserProfile.value = it.data
        }

        _isInlineLoading.value = it is Resource.Loading
    }

    init {
        liveUserProfileResource.observeForever(userProfileResourceObserver)
    }

    override fun onCleared() {
        super.onCleared()
        liveUserProfileResource.removeObserver(userProfileResourceObserver)
    }

    override fun linkFacebookAccount(accessTokenValue: String) {
        launchCatching {
            linkFacebookUsecase.linkFacebook(accessTokenValue)
            isFacebookAccountLinked.postValue(userAuthenticationState.isLinkedWithFacebook())
        }
    }

    override fun logout() {
        logoutUsecase.logout()
    }

    override fun unlinkProvider(unlinkProviderToken: ProviderToken<out ExternalAppToken>) {
        launchCatching {
            when (unlinkProviderToken.runningApp) {
                RunningApp.Strava -> deauthorizeStrava()
            }
        }
    }

    private suspend fun deauthorizeStrava() {
        deauthorizeStravaUsecase.deauthorizeStrava()
        removeStravaTokenUsecase.removeStravaTokenUsecase()
    }
}