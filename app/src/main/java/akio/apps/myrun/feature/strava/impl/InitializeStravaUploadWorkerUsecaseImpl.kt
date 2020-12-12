package akio.apps.myrun.feature.strava.impl

import akio.apps.myrun.data.activityfile.ActivityFileTrackingRepository
import akio.apps.myrun.data.activityfile.model.FileStatus
import akio.apps.myrun.data.activityfile.model.FileTarget
import akio.apps.myrun.data.externalapp.StravaTokenStorage
import akio.apps.myrun.feature.strava.InitializeStravaUploadWorkerUsecase
import android.content.Context
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.await
import javax.inject.Inject

class InitializeStravaUploadWorkerUsecaseImpl @Inject constructor(
    private val activityFileTrackingRepo: ActivityFileTrackingRepository,
    private val stravaTokenStorage: StravaTokenStorage,
    private val appContext: Context
) : InitializeStravaUploadWorkerUsecase {
    override suspend fun mayInitializeWorker() {
        if (stravaTokenStorage.getToken() == null) {
            val workManager = WorkManager.getInstance(appContext)
            workManager.getWorkInfosForUniqueWork(UploadStravaFileWorker.UNIQUE_WORK_NAME)
                .await()
                .forEach {
                    if (it.state != WorkInfo.State.CANCELLED) {
                        workManager.cancelWorkById(it.id)
                    }
                }

            return
        }

        activityFileTrackingRepo.countRecord(FileStatus.PENDING, FileTarget.STRAVA_UPLOAD)
            .takeIf { pendingRecordCount -> pendingRecordCount > 0 }
            ?.let {
                UploadStravaFileWorker.enqueueDaily(appContext)
            }
    }
}