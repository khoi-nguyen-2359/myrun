package akio.apps.myrun.domain.strava

import akio.apps.myrun.data.activity.api.ActivityLocalStorage
import akio.apps.myrun.data.authentication.api.UserAuthenticationState
import akio.apps.myrun.data.eapps.api.ExternalAppProvidersRepository
import akio.apps.myrun.data.eapps.api.StravaDataRepository
import akio.apps.myrun.wiring.common.NamedIoDispatcher
import javax.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.withContext
import timber.log.Timber

class UploadActivityFilesToStravaUsecase @Inject constructor(
    private val userAuthenticationState: UserAuthenticationState,
    private val externalAppProvidersRepository: ExternalAppProvidersRepository,
    private val stravaDataRepository: StravaDataRepository,
    private val activityLocalStorage: ActivityLocalStorage,
    @NamedIoDispatcher private val ioDispatcher: CoroutineDispatcher,
) {
    suspend fun uploadAll(): Boolean = withContext(ioDispatcher) {
        val userAccountId = userAuthenticationState.requireUserAccountId()
        val stravaToken = externalAppProvidersRepository.getStravaProviderToken(userAccountId)
        var isComplete = true
        if (stravaToken != null) {
            activityLocalStorage.loadAllActivitySyncDataFlow()
                .onEach { syncData ->
                    stravaDataRepository.saveActivity(
                        stravaToken,
                        syncData.activityModel.name,
                        syncData.tcxFile
                    )
                    activityLocalStorage.deleteActivitySyncData(syncData.activityModel.id)
                }
                .catch { exception ->
                    isComplete = false
                    Timber.e(exception)
                }
                .collect()
        }

        isComplete
    }
}
