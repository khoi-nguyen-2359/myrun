package akio.apps.myrun.feature.strava.impl

import akio.apps.myrun.data.activityfile.ActivityFileTrackingRepository
import akio.apps.myrun.data.activityfile.model.FileStatus
import akio.apps.myrun.data.activityfile.model.FileTarget
import akio.apps.myrun.data.authentication.UserAuthenticationState
import akio.apps.myrun.data.authentication.impl.UserAccount
import akio.apps.myrun.data.externalapp.ExternalAppProvidersRepository
import akio.apps.myrun.data.externalapp.StravaTokenStorage
import akio.apps.myrun.data.externalapp.model.ExternalProviders
import akio.apps.myrun.feature.strava.InitializeStravaUploadWorkerDelegate
import android.content.Context
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.await
import timber.log.Timber
import javax.inject.Inject

class InitializeStravaUploadWorkerDelegateImpl @Inject constructor(
    private val activityFileTrackingRepo: ActivityFileTrackingRepository,
    private val stravaTokenStorage: StravaTokenStorage,
    private val appContext: Context,
    private val authenticationState: UserAuthenticationState,
    private val externalAppProvidersRepository: ExternalAppProvidersRepository
) : InitializeStravaUploadWorkerDelegate {
    override suspend fun mayInitializeWorker() {
        val userAccount: UserAccount = authenticationState.getUserAccount()
            ?: return

        val providers: ExternalProviders =
            externalAppProvidersRepository.getExternalProviders(userAccount.uid)

        val stravaToken = providers.strava?.token
        if (stravaToken == null) {
            Timber.d("No strava token. Cancelling file upload workers...")
            stravaTokenStorage.clear()
            val workManager = WorkManager.getInstance(appContext)
            workManager.getWorkInfosForUniqueWork(UploadStravaFileWorker.UNIQUE_WORK_NAME)
                .await()
                .forEach {
                    if (it.state != WorkInfo.State.CANCELLED) {
                        it.id.let { workId ->
                            Timber.d("Cancelling work id=$workId")
                            workManager.cancelWorkById(workId)
                        }
                    }
                }

            return
        }

        stravaTokenStorage.setToken(stravaToken)
        Timber.d("Strava token found. Checking existing records.")
        activityFileTrackingRepo.countRecord(FileStatus.PENDING, FileTarget.STRAVA_UPLOAD)
            .takeIf { pendingRecordCount -> pendingRecordCount > 0 }
            ?.let { recordCount ->
                Timber.d("Records=$recordCount. Enqueue work.")
                UploadStravaFileWorker.enqueueDaily(appContext)
            }
    }
}
