package akio.apps.myrun.domain.activityexport

import akio.apps.myrun._di.NamedIoDispatcher
import akio.apps.myrun.data.activity.ActivityRepository
import akio.apps.myrun.data.activity.model.ActivityModel
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
    @NamedIoDispatcher private val ioDispatcher: CoroutineDispatcher
) {
    suspend operator fun invoke(activityModel: ActivityModel): File = withContext(ioDispatcher) {
        val tempExportFileDir = File("${application.cacheDir.absolutePath}/$PATH_TEMP_EXPORT_DIR")
        tempExportFileDir.mkdirs()
        val tempFile =
            File.createTempFile("exported_activity_${activityModel.id}", ".tcx", tempExportFileDir)

        val locations = activityRepository.getActivityLocationDataPoints(activityModel.id)

        activityTcxFileWriter.writeTcxFile(
            activity = activityModel,
            locations = locations.map {
                ActivityLocation(
                    activityModel.id,
                    it.timestamp,
                    it.value.latitude,
                    it.value.longitude,
                    it.value.altitude
                )
            },
            cadences = emptyList(),
            outputFile = tempFile,
            zip = false
        )

        return@withContext tempFile
    }

    companion object {
        private const val PATH_TEMP_EXPORT_DIR = "temp_export_file"
    }
}
