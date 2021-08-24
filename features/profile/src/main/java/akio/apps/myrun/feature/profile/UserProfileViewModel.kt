package akio.apps.myrun.feature.profile

import akio.apps.common.data.LaunchCatchingDelegate
import akio.apps.common.data.Resource
import akio.apps.myrun.data.activity.api.ActivityLocalStorage
import akio.apps.myrun.data.authentication.api.UserAuthenticationState
import akio.apps.myrun.data.eapps.api.model.ExternalProviders
import akio.apps.myrun.data.user.api.model.ProfileEditData
import akio.apps.myrun.data.user.api.model.UserProfile
import akio.apps.myrun.domain.strava.DeauthorizeStravaUsecase
import akio.apps.myrun.domain.strava.RemoveStravaTokenUsecase
import akio.apps.myrun.domain.user.GetProviderTokensUsecase
import akio.apps.myrun.domain.user.GetUserProfileUsecase
import akio.apps.myrun.domain.user.UpdateUserProfileUsecase
import akio.apps.myrun.worker.UploadStravaFileWorker
import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class UserProfileViewModel @Inject constructor(
    private val application: Application,
    private val arguments: Arguments,
    private val getUserProfileUsecase: GetUserProfileUsecase,
    private val getProviderTokensUsecase: GetProviderTokensUsecase,
    private val deauthorizeStravaUsecase: DeauthorizeStravaUsecase,
    private val removeStravaTokenUsecase: RemoveStravaTokenUsecase,
    private val userAuthenticationState: UserAuthenticationState,
    private val activityLocalStorage: ActivityLocalStorage,
    private val launchCatchingDelegate: LaunchCatchingDelegate,
    private val updateUserProfileUsecase: UpdateUserProfileUsecase,
) : ViewModel(), LaunchCatchingDelegate by launchCatchingDelegate {

    val userProfileResourceFlow: Flow<Resource<UserProfile>> =
        getUserProfileUsecase.getUserProfileFlow(arguments.userId)

    val liveProviders: Flow<Resource<out ExternalProviders>> =
        getProviderTokensUsecase.getProviderTokensFlow()

    suspend fun getActivityUploadCount(): Int =
        activityLocalStorage.getActivityStorageDataCountFlow().first()

    fun isCurrentUser(): Boolean =
        arguments.userId == userAuthenticationState.getUserAccountId() ||
            arguments.userId == null

    fun deauthorizeStrava() {
        viewModelScope.launchCatching {
            deauthorizeStravaUsecase.deauthorizeStrava()
            removeStravaTokenUsecase.removeStravaToken()

            UploadStravaFileWorker.clear(application)
        }
    }

    fun updateUserProfile(profileEditData: ProfileEditData) {
        updateUserProfileUsecase.updateUserProfile(profileEditData)
    }

    data class Arguments(val userId: String?)
}
