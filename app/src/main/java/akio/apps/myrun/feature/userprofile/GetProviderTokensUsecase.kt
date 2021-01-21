package akio.apps.myrun.feature.userprofile

import akio.apps._base.data.Resource
import akio.apps.myrun.data.externalapp.model.ExternalProviders
import androidx.lifecycle.LiveData

interface GetProviderTokensUsecase {
    fun getProviderTokens(): LiveData<Resource<ExternalProviders>>
}
