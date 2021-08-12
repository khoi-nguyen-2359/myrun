package akio.apps.myrun.feature.userprofile.impl

import akio.apps.common.data.Resource
import akio.apps.common.feature.lifecycle.Event
import akio.apps.myrun.data.activity.api.ActivityLocalStorage
import akio.apps.myrun.data.authentication.api.UserAuthenticationState
import akio.apps.myrun.data.externalapp.model.ExternalAppToken
import akio.apps.myrun.data.externalapp.model.ProviderToken
import akio.apps.myrun.data.externalapp.model.RunningApp
import akio.apps.myrun.data.user.api.model.UserProfile
import akio.apps.myrun.domain.strava.DeauthorizeStravaUsecase
import akio.apps.myrun.domain.strava.RemoveStravaTokenUsecase
import akio.apps.myrun.domain.user.GetProviderTokensUsecase
import akio.apps.myrun.domain.user.GetUserProfileUsecase
import akio.apps.myrun.feature.strava.impl.UploadStravaFileWorker
import akio.apps.myrun.feature.userprofile.UserProfileViewModel
import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.asLiveData
import androidx.work.WorkManager
import javax.inject.Inject
import kotlinx.coroutines.flow.first

class UserProfileViewModelImpl @Inject constructor(
    private val application: Application,
    private val params: Params,
    private val getUserProfileUsecase: GetUserProfileUsecase,
    private val getProviderTokensUsecase: GetProviderTokensUsecase,
    private val deauthorizeStravaUsecase: DeauthorizeStravaUsecase,
    private val removeStravaTokenUsecase: RemoveStravaTokenUsecase,
    private val logoutDelegate: UserLogoutDelegate,
    private val userAuthenticationState: UserAuthenticationState,
    private val activityLocalStorage: ActivityLocalStorage,
) : UserProfileViewModel() {

    private val _isInlineLoading = MutableLiveData<Boolean>()
    override val isInlineLoading: LiveData<Boolean> = _isInlineLoading

    private val liveUserProfile = MutableLiveData<UserProfile>()
    override fun getUserProfileAlive(): LiveData<UserProfile> = liveUserProfile

    private val liveUserProfileResource = getUserProfileUsecase.getUserProfileFlow(params.userId)
        .asLiveData(timeoutInMs = 0)

    private val liveProviders = getProviderTokensUsecase.getProviderTokensFlow()
        .asLiveData(timeoutInMs = 0)

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

    override suspend fun logout() {
        liveUserProfileResource.removeObserver(userProfileResourceObserver)
        logoutDelegate(application)
    }

    override suspend fun getActivityUploadCount(): Int =
        activityLocalStorage.getActivityStorageDataCountFlow().first()

    override fun isCurrentUser(): Boolean =
        params.userId == userAuthenticationState.getUserAccountId() ||
            params.userId == null

    override fun unlinkProvider(unlinkProviderToken: ProviderToken<out ExternalAppToken>) {
        launchCatching {
            when (unlinkProviderToken.runningApp) {
                RunningApp.Strava -> deauthorizeStrava()
            }
        }
    }

    private suspend fun deauthorizeStrava() {
        deauthorizeStravaUsecase.deauthorizeStrava()
        removeStravaTokenUsecase.removeStravaToken()
        WorkManager.getInstance(application)
            .cancelUniqueWork(UploadStravaFileWorker.UNIQUE_WORK_NAME)
    }
}
