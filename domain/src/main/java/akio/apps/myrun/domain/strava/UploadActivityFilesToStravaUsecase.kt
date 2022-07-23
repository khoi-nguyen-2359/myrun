package akio.apps.myrun.domain.strava

import akio.apps.myrun.data.activity.api.ActivityLocalStorage
import akio.apps.myrun.data.authentication.api.UserAuthenticationState
import akio.apps.myrun.data.eapps.api.ExternalAppProvidersRepository
import akio.apps.myrun.data.eapps.api.StravaDataRepository
import akio.apps.myrun.data.eapps.api.StravaSyncState
import javax.inject.Inject
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.flowOf
import timber.log.Timber

class UploadActivityFilesToStravaUsecase @Inject constructor(
    private val userAuthenticationState: UserAuthenticationState,
    private val externalAppProvidersRepository: ExternalAppProvidersRepository,
    private val stravaDataRepository: StravaDataRepository,
    private val stravaSyncState: StravaSyncState,
    private val activityLocalStorage: ActivityLocalStorage,
) {
    @OptIn(FlowPreview::class)
    suspend fun uploadAll(): Boolean {
        val userAccountId = userAuthenticationState.requireUserAccountId()
        val stravaToken = externalAppProvidersRepository.getStravaProviderToken(userAccountId)
        if (stravaToken == null) {
            stravaSyncState.setStravaSyncAccountId(null)
        }
        return activityLocalStorage.loadAllActivitySyncDataFlow().flatMapConcat { syncData ->
            if (stravaToken != null) {
                stravaDataRepository.saveActivity(
                    stravaToken,
                    syncData.activityModel.name,
                    syncData.tcxFile
                )
            }
            activityLocalStorage.deleteActivitySyncData(syncData.activityModel.id)
            flowOf(true)
        }
            .catch { exception ->
                emit(false)
                Timber.e(exception)
            }
            .firstOrNull { !it } ?: true
    }
}
