package akio.apps.myrun.domain.activityexport

import akio.apps.myrun._di.NamedIoDispatcher
import akio.apps.myrun.data.activity.ActivityRepository
import akio.apps.myrun.data.activityexport.ExportActivityLocationCache
import akio.apps.myrun.data.activityexport.model.ActivityLocation
import android.app.Application
import java.io.File
import javax.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

class ExportActivityToTempTcxFileUsecase @Inject constructor(
    private val application: Application,
    private val activityTcxFileWriter: ActivityTcxFileWriter,
    private val activityRepository: ActivityRepository,
    private val exportActivityLocationCache: ExportActivityLocationCache,
    @NamedIoDispatcher private val ioDispatcher: CoroutineDispatcher
) {
    /**
     * Exports content the of activity with given [activityId] to a TCX file. Returns null if error
     * happened.
     */
    suspend operator fun invoke(activityId: String): File? =
        withContext(ioDispatcher) {
            val tempFile = File.createTempFile("activity_$activityId", ".tcx", application.cacheDir)
            tempFile.deleteOnExit()
            val activityModel =
                activityRepository.getActivity(activityId) ?: return@withContext null
            val activityLocations = getActivityLocations(activityId)
            activityTcxFileWriter.writeTcxFile(
                activity = activityModel,
                locations = activityLocations,
                cadences = emptyList(),
                outputFile = tempFile,
                zip = false
            )
            tempFile
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
