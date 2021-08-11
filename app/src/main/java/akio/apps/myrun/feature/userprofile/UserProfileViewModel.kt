package akio.apps.myrun.feature.userprofile

import akio.apps.common.data.Resource
import akio.apps.common.feature.viewmodel.BaseViewModel
import akio.apps.myrun.data.externalapp.model.ExternalAppToken
import akio.apps.myrun.data.externalapp.model.ExternalProviders
import akio.apps.myrun.data.externalapp.model.ProviderToken
import akio.apps.myrun.data.user.api.model.UserProfile
import androidx.lifecycle.LiveData

abstract class UserProfileViewModel : BaseViewModel() {
    abstract val isInlineLoading: LiveData<Boolean>
    abstract fun getUserProfileAlive(): LiveData<UserProfile>
    abstract fun getProvidersAlive(): LiveData<akio.apps.common.data.Resource<ExternalProviders>>
    abstract fun unlinkProvider(unlinkProviderToken: ProviderToken<out ExternalAppToken>)
    abstract suspend fun logout()
    abstract fun isCurrentUser(): Boolean
    abstract suspend fun getActivityUploadCount(): Int
    data class Params(val userId: String?)
}
