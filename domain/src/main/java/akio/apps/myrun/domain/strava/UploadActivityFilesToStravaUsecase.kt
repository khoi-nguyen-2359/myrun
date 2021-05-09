package akio.apps.myrun.domain.strava

import akio.apps.myrun._di.NamedIoDispatcher
import akio.apps.myrun.data.activityexport.ActivityFileTrackingRepository
import akio.apps.myrun.data.activityexport.model.FileStatus
import akio.apps.myrun.data.activityexport.model.FileTarget
import akio.apps.myrun.data.authentication.UserAuthenticationState
import akio.apps.myrun.data.externalapp.ExternalAppProvidersRepository
import akio.apps.myrun.data.externalapp.StravaDataRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject

class UploadActivityFilesToStravaUsecase @Inject constructor(
    private val userAuthenticationState: UserAuthenticationState,
    private val externalAppProvidersRepository: ExternalAppProvidersRepository,
    private val stravaDataRepository: StravaDataRepository,
    private val activityFileTrackingRepository: ActivityFileTrackingRepository,
    @NamedIoDispatcher private val ioDispatcher: CoroutineDispatcher
) {
    suspend fun upload(): Int = withContext(ioDispatcher) {
        val userAccountId = userAuthenticationState.getUserAccountId()
        val stravaToken = if (userAccountId == null)
            null
        else
            externalAppProvidersRepository.getStravaProviderToken(userAccountId)

        if (stravaToken != null) {
            val allRecords = activityFileTrackingRepository.getRecords(
                FileStatus.PENDING,
                FileTarget.STRAVA_UPLOAD
            )
            allRecords.forEach { trackingRecord ->
                try {
                    activityFileTrackingRepository.updateStatus(
                        trackingRecord.recordId,
                        FileStatus.PROCESSING
                    )
                    stravaDataRepository.saveActivity(
                        stravaToken,
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
        }

        return@withContext activityFileTrackingRepository.countRecord(
            FileStatus.PENDING,
            FileTarget.STRAVA_UPLOAD
        )
    }
}
