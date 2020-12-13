package akio.apps.myrun.feature.userprofile.impl

import akio.apps._base.data.Resource
import akio.apps._base.lifecycle.Event
import akio.apps.myrun.data.externalapp.model.ExternalAppToken
import akio.apps.myrun.data.externalapp.model.ProviderToken
import akio.apps.myrun.data.externalapp.model.RunningApp
import akio.apps.myrun.data.userprofile.model.UserProfile
import akio.apps.myrun.feature.strava.impl.UploadStravaFileWorker
import akio.apps.myrun.feature.userprofile.DeauthorizeStravaUsecase
import akio.apps.myrun.feature.userprofile.GetProviderTokensUsecase
import akio.apps.myrun.feature.userprofile.GetUserProfileUsecase
import akio.apps.myrun.feature.userprofile.LogoutUsecase
import akio.apps.myrun.feature.userprofile.RemoveStravaTokenUsecase
import akio.apps.myrun.feature.userprofile.UserProfileViewModel
import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.asLiveData
import androidx.work.WorkManager
import javax.inject.Inject

class UserProfileViewModelImpl @Inject constructor(
    private val getUserProfileUsecase: GetUserProfileUsecase,
    private val getProviderTokensUsecase: GetProviderTokensUsecase,
    private val deauthorizeStravaUsecase: DeauthorizeStravaUsecase,
    private val removeStravaTokenUsecase: RemoveStravaTokenUsecase,
    private val logoutUsecase: LogoutUsecase,
    val appContext: Context
) : UserProfileViewModel() {

    private val _isInlineLoading = MutableLiveData<Boolean>()
    override val isInlineLoading: LiveData<Boolean> = _isInlineLoading

    private val liveUserProfile = MutableLiveData<UserProfile>()
    override fun getUserProfileAlive(): LiveData<UserProfile> = liveUserProfile

    private val liveUserProfileResource = getUserProfileUsecase.getUserProfileFlow()
        .asLiveData(timeoutInMs = 0)

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

    override fun logout() {
        launchCatching {
            logoutUsecase.logout()
        }
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

        WorkManager.getInstance(appContext)
            .cancelUniqueWork(UploadStravaFileWorker.UNIQUE_WORK_NAME)
    }
}
