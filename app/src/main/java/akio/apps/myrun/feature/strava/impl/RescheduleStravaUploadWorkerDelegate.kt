package akio.apps.myrun.feature.strava.impl

import akio.apps.myrun.data.activitysharing.ActivityFileTrackingRepository
import akio.apps.myrun.data.activitysharing.model.FileStatus
import akio.apps.myrun.data.activitysharing.model.FileTarget
import akio.apps.myrun.data.authentication.UserAuthenticationState
import akio.apps.myrun.data.externalapp.ExternalAppProvidersRepository
import android.app.Application
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.await
import timber.log.Timber
import javax.inject.Inject

class RescheduleStravaUploadWorkerDelegate @Inject constructor(
    private val activityFileTrackingRepository: ActivityFileTrackingRepository,
    private val externalAppProvidersRepository: ExternalAppProvidersRepository,
    private val userAuthenticationState: UserAuthenticationState,
    private val application: Application
) {
    suspend fun rescheduleWorker() {
        val userAccountId = userAuthenticationState.getUserAccountId()
        if (userAccountId == null ||
            externalAppProvidersRepository.getStravaProviderToken(userAccountId) == null
        ) {
            Timber.d("No strava token. Cancelling file upload workers...")
            val workManager = WorkManager.getInstance(application)
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

        Timber.d("Strava token found. Checking existing records.")
        activityFileTrackingRepository.countRecord(FileStatus.PENDING, FileTarget.STRAVA_UPLOAD)
            .takeIf { pendingRecordCount -> pendingRecordCount > 0 }
            ?.let { recordCount ->
                Timber.d("Records=$recordCount. Enqueue work.")
                UploadStravaFileWorker.enqueueDaily(application)
            }
    }
}