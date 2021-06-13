package akio.apps.myrun.domain.strava

import akio.apps.myrun._di.NamedIoDispatcher
import akio.apps.myrun.data.activitysharing.ActivityLocalStorage
import akio.apps.myrun.data.authentication.UserAuthenticationState
import akio.apps.myrun.data.externalapp.ExternalAppProvidersRepository
import akio.apps.myrun.data.externalapp.StravaDataRepository
import timber.log.Timber
import javax.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.withContext

class UploadActivityFilesToStravaUsecase @Inject constructor(
    private val userAuthenticationState: UserAuthenticationState,
    private val externalAppProvidersRepository: ExternalAppProvidersRepository,
    private val stravaDataRepository: StravaDataRepository,
    private val activityLocalStorage: ActivityLocalStorage,
    @NamedIoDispatcher private val ioDispatcher: CoroutineDispatcher
) {
    suspend fun uploadAll(): Boolean = withContext(ioDispatcher) {
        val userAccountId = userAuthenticationState.requireUserAccountId()
        val stravaToken = externalAppProvidersRepository.getStravaProviderToken(userAccountId)

        var isComplete = true
        if (stravaToken != null) {
            activityLocalStorage.loadAllActivitySyncDataFlow()
                .catch { isComplete = false }
                .collect { syncData ->
                    try {
                        stravaDataRepository.saveActivity(
                            stravaToken,
                            syncData.activityModel.name,
                            syncData.tcxFile
                        )
                        activityLocalStorage.deleteActivitySyncData(syncData.activityModel.id)
                    } catch (ex: Exception) {
                        Timber.e(ex)
                    }
                }
        }

        isComplete
    }
}
