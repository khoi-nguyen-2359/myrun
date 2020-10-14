package akio.apps.myrun.feature.userprofile

import akio.apps._base.data.Resource
import akio.apps._base.viewmodel.BaseViewModel
import akio.apps.myrun.data.externalapp.model.ExternalAppToken
import akio.apps.myrun.data.externalapp.model.ExternalProviders
import akio.apps.myrun.data.externalapp.model.ProviderToken
import akio.apps.myrun.data.userprofile.model.UserProfile
import androidx.lifecycle.LiveData

abstract class UserProfileViewModel: BaseViewModel() {
    abstract val isInlineLoading: LiveData<Boolean>
    abstract fun getUserProfileAlive(): LiveData<UserProfile>
    abstract fun isFacebookAccountLinked(): LiveData<Boolean>
    abstract fun getProvidersAlive(): LiveData<Resource<ExternalProviders>>
    abstract fun unlinkProvider(unlinkProviderToken: ProviderToken<out ExternalAppToken>)
    abstract fun logout()
    abstract fun linkFacebookAccount(accessTokenValue: String)
}