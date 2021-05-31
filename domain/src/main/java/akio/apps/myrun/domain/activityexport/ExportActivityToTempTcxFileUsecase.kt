package akio.apps.myrun.domain.activityexport

import akio.apps.myrun._di.NamedIoDispatcher
import akio.apps.myrun.data.activity.ActivityRepository
import akio.apps.myrun.data.activityexport.ActivityFileTrackingRepository
import akio.apps.myrun.data.activityexport.ExportActivityLocationCache
import akio.apps.myrun.data.activityexport.model.ActivityLocation
import akio.apps.myrun.data.activityexport.model.FileStatus
import akio.apps.myrun.data.activityexport.model.FileTarget
import akio.apps.myrun.data.activityexport.model.TrackingRecord
import javax.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

class ExportActivityToTempTcxFileUsecase @Inject constructor(
    private val activityTcxFileWriter: ActivityTcxFileWriter,
    private val activityRepository: ActivityRepository,
    private val activityFileTrackingRepository: ActivityFileTrackingRepository,
    private val exportActivityLocationCache: ExportActivityLocationCache,
    @NamedIoDispatcher private val ioDispatcher: CoroutineDispatcher
) {
    /**
     * Exports content the of activity with given [activityId] to a TCX file. Returns null if error
     * happened.
     */
    suspend operator fun invoke(activityId: String): TrackingRecord? = withContext(ioDispatcher) {
        val tempFile = activityFileTrackingRepository.createEmptyFile(activityId)
        val activityModel = activityRepository.getActivity(activityId) ?: return@withContext null
        val activityLocations = getActivityLocations(activityId)
        activityTcxFileWriter.writeTcxFile(
            activity = activityModel,
            locations = activityLocations,
            cadences = emptyList(),
            outputFile = tempFile,
            zip = false
        )
        return@withContext activityFileTrackingRepository.track(
            activityId,
            activityModel.name,
            activityModel.startTime,
            tempFile,
            FileTarget.TRACKLOG,
            FileStatus.PROCESSING
        )
    }

    private suspend fun getActivityLocations(activityId: String): List<ActivityLocation> {
        val cachedActivityLocations = exportActivityLocationCache.getActivityLocations(activityId)
        if (cachedActivityLocations.isNotEmpty()) {
            return cachedActivityLocations
        }
        return activityRepository.getActivityLocationDataPoints(activityId)
            .map {
                ActivityLocation(
                    activityId,
                    it.timestamp,
                    it.value.latitude,
                    it.value.longitude,
                    it.value.altitude
                )
            }
    }
}
