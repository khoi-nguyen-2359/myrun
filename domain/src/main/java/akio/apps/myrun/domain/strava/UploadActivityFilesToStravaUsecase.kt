package akio.apps.myrun.domain.strava

import akio.apps.myrun.data.activityfile.ActivityFileTrackingRepository
import akio.apps.myrun.data.activityfile.model.FileStatus
import akio.apps.myrun.data.activityfile.model.FileTarget
import akio.apps.myrun.data.externalapp.StravaDataRepository
import timber.log.Timber
import javax.inject.Inject

class UploadActivityFilesToStravaUsecase @Inject constructor(
    private val stravaDataRepository: StravaDataRepository,
    private val activityFileTrackingRepository: ActivityFileTrackingRepository
) {
    suspend fun upload(): Int {
        val allRecords =
            activityFileTrackingRepository.getRecords(FileStatus.PENDING, FileTarget.STRAVA_UPLOAD)
        allRecords.forEach { trackingRecord ->
            try {
                activityFileTrackingRepository.updateStatus(
                    trackingRecord.recordId,
                    FileStatus.PROCESSING
                )
                stravaDataRepository.saveActivity(
                    trackingRecord.activityName,
                    trackingRecord.activityFile
                )
                activityFileTrackingRepository.updateStatus(
                    trackingRecord.recordId,
                    FileStatus.SUCCEED
                )
                activityFileTrackingRepository.delete(trackingRecord.recordId)
            } catch (ex: Exception) {
                Timber.e(ex)
                activityFileTrackingRepository.updateStatus(
                    trackingRecord.recordId,
                    FileStatus.PENDING
                )
            }
        }

        return activityFileTrackingRepository.countRecord(
            FileStatus.PENDING,
            FileTarget.STRAVA_UPLOAD
        )
    }
}
