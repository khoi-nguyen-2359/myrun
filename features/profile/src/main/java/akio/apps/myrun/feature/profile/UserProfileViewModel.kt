package akio.apps.myrun.feature.profile

import akio.apps.common.feature.viewmodel.LaunchCatchingDelegate
import akio.apps.myrun.data.activity.api.ActivityLocalStorage
import akio.apps.myrun.data.authentication.api.UserAuthenticationState
import akio.apps.myrun.data.eapps.api.model.ExternalAppToken
import akio.apps.myrun.data.eapps.api.model.ProviderToken
import akio.apps.myrun.data.eapps.api.model.RunningApp.Strava
import akio.apps.myrun.domain.strava.DeauthorizeStravaUsecase
import akio.apps.myrun.domain.strava.RemoveStravaTokenUsecase
import akio.apps.myrun.domain.user.GetProviderTokensUsecase
import akio.apps.myrun.domain.user.GetUserProfileUsecase
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import javax.inject.Inject
import kotlinx.coroutines.flow.first

class UserProfileViewModel @Inject constructor(
    private val arguments: Arguments,
    private val getUserProfileUsecase: GetUserProfileUsecase,
    private val getProviderTokensUsecase: GetProviderTokensUsecase,
    private val deauthorizeStravaUsecase: DeauthorizeStravaUsecase,
    private val removeStravaTokenUsecase: RemoveStravaTokenUsecase,
    private val logoutDelegate: UserLogoutDelegate,
    private val userAuthenticationState: UserAuthenticationState,
    private val activityLocalStorage: ActivityLocalStorage,
    private val launchCatchingDelegate: LaunchCatchingDelegate,
) : ViewModel(), LaunchCatchingDelegate by launchCatchingDelegate {

    val userProfileResourceFlow = getUserProfileUsecase.getUserProfileFlow(arguments.userId)
//        .shareIn(viewModelScope, SharingStarted.WhileSubscribed(), 1)
//    val userProfileFlow: Flow<UserProfile> = userProfileResourceFlow.mapNotNull { it.data }
//    val userProfileErrorFlow: Flow<Throwable> = userProfileResourceFlow.mapNotNull {
//        (it as? Resource.Error)?.exception
//    }
//    val isUserProfileLoadingFlow: Flow<Boolean> = userProfileResourceFlow.map {
//        it is Resource.Loading
//    }

    val liveProviders = getProviderTokensUsecase.getProviderTokensFlow()

    suspend fun logout() {
//        logoutDelegate(application)
    }

    suspend fun getActivityUploadCount(): Int =
        activityLocalStorage.getActivityStorageDataCountFlow().first()

    fun isCurrentUser(): Boolean =
        arguments.userId == userAuthenticationState.getUserAccountId() ||
            arguments.userId == null

    fun unlinkProvider(unlinkProviderToken: ProviderToken<out ExternalAppToken>) {
        viewModelScope.launchCatching {
            when (unlinkProviderToken.runningApp) {
                Strava -> deauthorizeStrava()
            }
        }
    }

    private suspend fun deauthorizeStrava() {
        deauthorizeStravaUsecase.deauthorizeStrava()
        removeStravaTokenUsecase.removeStravaToken()
        // TODO: react on this event to clear worker
//        WorkManager.getInstance(application)
//            .cancelUniqueWork(UploadStravaFileWorker.UNIQUE_WORK_NAME)
    }

    data class Arguments(val userId: String?)
}
