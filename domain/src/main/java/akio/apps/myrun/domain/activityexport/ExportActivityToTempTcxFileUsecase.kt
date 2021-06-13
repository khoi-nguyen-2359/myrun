package akio.apps.myrun.domain.activityexport

import akio.apps.myrun._di.NamedIoDispatcher
import akio.apps.myrun.data.activity.ActivityRepository
import akio.apps.myrun.data.activity.ActivityTcxFileWriter
import akio.apps.myrun.data.activity.model.ActivityModel
import akio.apps.myrun.data.activitysharing.model.ActivityLocation
import android.app.Application
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

class ExportActivityToTempTcxFileUsecase @Inject constructor(
    private val application: Application,
    private val activityTcxFileWriter: ActivityTcxFileWriter,
    private val activityRepository: ActivityRepository,
    @NamedIoDispatcher private val ioDispatcher: CoroutineDispatcher
) {
    private val timeFormatter = SimpleDateFormat("ddMMyyyy_HHmm", Locale.US)

    /**
     * Exports content the of activity with given [activityId] to a TCX file. Returns null if error
     * happened.
     */
    suspend operator fun invoke(activityId: String): File? = withContext(ioDispatcher) {
        val activityModel = activityRepository.getActivity(activityId)
            ?: return@withContext null
        val fileName = makeFileName(activityModel)
        val externalFileDir = application.getExternalFilesDir(null)
        val storeDir = if (externalFileDir != null) {
            File("${externalFileDir.absolutePath}/$DIR_NAME")
        } else {
            File("${application.cacheDir.absolutePath}/$DIR_NAME")
        }
        storeDir.mkdirs()
        val storeFile = File("${storeDir.absolutePath}/$fileName")
        if (storeFile.isFile && storeFile.exists()) {
            return@withContext storeFile
        }

        storeFile.createNewFile()
        if (externalFileDir == null) {
            // delete cache file after used
            storeFile.deleteOnExit()
        }
        val activityLocations = getActivityLocations(activityId)
        activityTcxFileWriter.writeTcxFile(
            activity = activityModel,
            locations = activityLocations,
            cadences = emptyList(),
            outputFile = storeFile,
            zip = false
        )
        storeFile
    }

    private fun makeFileName(activity: ActivityModel): String =
        "${activity.activityType.name}_${timeFormatter.format(Date(activity.startTime))}.tcx"

    private suspend fun getActivityLocations(activityId: String): List<ActivityLocation> {
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

    companion object {
        private const val DIR_NAME = "Exported Activities"
    }
}
