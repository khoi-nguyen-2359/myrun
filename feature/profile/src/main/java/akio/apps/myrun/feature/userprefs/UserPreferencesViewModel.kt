package akio.apps.myrun.feature.userprefs

import akio.apps.myrun.base.di.NamedIoDispatcher
import akio.apps.myrun.data.authentication.api.SignInManager
import akio.apps.myrun.data.authentication.api.UserAuthenticationState
import akio.apps.myrun.data.common.Resource
import akio.apps.myrun.data.eapps.api.model.ExternalProviders
import akio.apps.myrun.data.user.api.CurrentUserPreferences
import akio.apps.myrun.data.user.api.UserPreferencesRepository
import akio.apps.myrun.data.user.api.model.MeasureSystem
import akio.apps.myrun.domain.strava.DeauthorizeStravaUsecase
import akio.apps.myrun.domain.user.GetProviderTokensUsecase
import akio.apps.myrun.feature.core.launchcatching.LaunchCatchingDelegate
import akio.apps.myrun.worker.UploadStravaFileWorker
import android.app.Application
import javax.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class UserPreferencesViewModel @Inject constructor(
    private val application: Application,
    private val launchCatching: LaunchCatchingDelegate,
    private val getProviderTokensUsecase: GetProviderTokensUsecase,
    private val deauthorizeStravaUsecase: DeauthorizeStravaUsecase,
    private val currentUserPreferences: CurrentUserPreferences,
    private val signInManager: SignInManager,
    @NamedIoDispatcher
    private val ioDispatcher: CoroutineDispatcher,
    private val viewModelScope: CoroutineScope,
    private val userPrefsRepo: UserPreferencesRepository,
    private val authState: UserAuthenticationState,
) : LaunchCatchingDelegate by launchCatching {

    private val isAccountDeletedFlow: MutableStateFlow<Boolean> = MutableStateFlow(false)

    private val userId: String = authState.requireUserAccountId()

    val screenState: StateFlow<ScreenState> = combine(
        currentUserPreferences.getMeasureSystemFlow(),
        getProviderTokensUsecase.getProviderTokensFlow()
            .map(::mapAppTokensToStravaLinkState)
            .flowOn(ioDispatcher),
        combine(launchCatchingLoading, launchCatchingError, ::Pair),
        isAccountDeletedFlow,
        userPrefsRepo.getUserPreferencesFlow(userId).map { it.showActivityMapOnFeed }
            .flowOn(ioDispatcher),
        ::ScreenState
    ).stateIn(viewModelScope, SharingStarted.WhileSubscribed(), ScreenState())

    fun selectMeasureSystem(measureSystem: MeasureSystem) = viewModelScope.launch {
        currentUserPreferences.setMeasureSystem(measureSystem)
    }

    fun deauthorizeStrava() = viewModelScope.launchCatching {
        withContext(ioDispatcher) { deauthorizeStravaUsecase.deauthorizeStrava() }

        UploadStravaFileWorker.clear(application)
    }

    fun deleteUser() = viewModelScope.launchCatching {
        withContext(ioDispatcher) {
            signInManager.deleteUserAccount()
        }
        isAccountDeletedFlow.value = true
    }

    fun acknowledgeError() {
        setLaunchCatchingError(null) // this makes screen state re-combine with null error
    }

    private fun mapAppTokensToStravaLinkState(
        appTokensResource: Resource<out ExternalProviders>,
    ) = when {
        appTokensResource is Resource.Error -> StravaLinkState.Unknown
        appTokensResource.data?.strava != null -> StravaLinkState.Linked
        appTokensResource.data?.strava == null -> StravaLinkState.NotLinked
        else -> StravaLinkState.Unknown
    }

    fun toggleShowActivityMapOnFeed() {
        userPrefsRepo.setShowActivityMapOnFeed(userId, !screenState.value.showActivityMapOnFeed)
    }

    enum class StravaLinkState { Linked, NotLinked, Unknown }

    data class ScreenState(
        val measureSystem: MeasureSystem = MeasureSystem.Default,
        val stravaLinkState: StravaLinkState = StravaLinkState.Unknown,
        val loadingErrorState: Pair<Boolean, Throwable?> = Pair(false, null),
        val isAccountDeleted: Boolean = false,
        val showActivityMapOnFeed: Boolean = true,
    )
}
