package akio.apps.myrun.feature.userprefs

import akio.apps.myrun.base.di.NamedIoDispatcher
import akio.apps.myrun.data.authentication.api.SignInManager
import akio.apps.myrun.data.authentication.api.error.UnauthorizedUserError
import akio.apps.myrun.data.common.Resource
import akio.apps.myrun.data.eapps.api.model.ExternalProviders
import akio.apps.myrun.data.user.api.UserPreferences
import akio.apps.myrun.data.user.api.model.MeasureSystem
import akio.apps.myrun.domain.strava.DeauthorizeStravaUsecase
import akio.apps.myrun.domain.user.GetProviderTokensUsecase
import akio.apps.myrun.feature.core.launchcatching.LaunchCatchingDelegate
import akio.apps.myrun.worker.UploadStravaFileWorker
import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import javax.inject.Inject
import kotlin.jvm.Throws
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class UserPreferencesViewModel @Inject constructor(
    private val application: Application,
    private val launchCatching: LaunchCatchingDelegate,
    private val getProviderTokensUsecase: GetProviderTokensUsecase,
    private val deauthorizeStravaUsecase: DeauthorizeStravaUsecase,
    private val userPreferences: UserPreferences,
    private val signInManager: SignInManager,
    @NamedIoDispatcher
    private val ioDispatcher: CoroutineDispatcher,
) : ViewModel(), LaunchCatchingDelegate by launchCatching {

    val preferredSystem: Flow<MeasureSystem> = userPreferences.getMeasureSystem()
    val stravaLinkState: Flow<StravaLinkState> = getProviderTokensUsecase.getProviderTokensFlow()
        .map(::mapAppTokensToStravaLinkState)
        .flowOn(ioDispatcher)

    fun selectMeasureSystem(measureSystem: MeasureSystem) = viewModelScope.launch {
        userPreferences.setMeasureSystem(measureSystem)
    }

    fun deauthorizeStrava() = viewModelScope.launchCatching {
        withContext(ioDispatcher) { deauthorizeStravaUsecase.deauthorizeStrava() }

        UploadStravaFileWorker.clear(application)
    }

    @Throws(UnauthorizedUserError::class)
    suspend fun deleteUser() = withContext(ioDispatcher) {
        signInManager.deleteUserAccount()
    }

    private fun mapAppTokensToStravaLinkState(
        appTokensResource: Resource<out ExternalProviders>,
    ) = when {
        appTokensResource is Resource.Error -> StravaLinkState.Unknown
        appTokensResource.data?.strava != null -> StravaLinkState.Linked
        appTokensResource.data?.strava == null -> StravaLinkState.NotLinked
        else -> StravaLinkState.Unknown
    }

    enum class StravaLinkState { Linked, NotLinked, Unknown }
}
