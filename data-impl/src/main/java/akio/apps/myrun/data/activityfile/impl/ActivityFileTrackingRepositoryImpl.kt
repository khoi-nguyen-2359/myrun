package akio.apps.myrun.data.activityfile.impl

import akio.apps.myrun._di.NamedIoDispatcher
import akio.apps.myrun.data.activityfile.ActivityFileTrackingRepository
import akio.apps.myrun.data.activityfile.entity.ActivityFileRecord
import akio.apps.myrun.data.activityfile.model.FileStatus
import akio.apps.myrun.data.activityfile.model.FileTarget
import akio.apps.myrun.data.activityfile.model.TrackingRecord
import android.app.Application
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

class ActivityFileTrackingRepositoryImpl @Inject constructor(
    application: Application,
    activityFileDatabase: ActivityFileDatabase,
    @NamedIoDispatcher private val ioDispatcher: CoroutineDispatcher
) : ActivityFileTrackingRepository {

    private val contentDir: File = File(application.filesDir, "activity/")

    private val activityFilePrefix = "activity_"

    private val activityFileTrackingDao = activityFileDatabase.activityFileDao()

    init {
        initContentDir()
    }

    private fun initContentDir() = if (!contentDir.exists()) {
        contentDir.mkdirs()
    } else
        false

    override fun createEmptyFile(activityId: String): File {
        return File.createTempFile("$activityFilePrefix$activityId", null, contentDir)
    }

    override suspend fun track(
        activityId: String,
        activityName: String,
        activityFile: File,
        target: FileTarget
    ) = withContext(ioDispatcher) {
        val record = ActivityFileRecord(
            id = 0,
            activityId = activityId,
            activityName = activityName,
            filePath = activityFile.absolutePath,
            status = FileStatus.PENDING.name,
            target = target.name
        )
        activityFileTrackingDao.insert(record)
    }

    override suspend fun updateStatus(fileId: Int, status: FileStatus) = withContext(ioDispatcher) {
        activityFileTrackingDao.updateRecord(fileId, status.name)
    }

    override suspend fun delete(fileId: Int): Unit = withContext(ioDispatcher) {
        val filePath = activityFileTrackingDao.getFilePath(fileId)
        activityFileTrackingDao.delete(fileId)
        File(filePath).delete()
    }

    override suspend fun countRecord(status: FileStatus, target: FileTarget): Int =
        withContext(ioDispatcher) {
            activityFileTrackingDao.count(status.name, target.name)
        }

    override suspend fun getRecords(status: FileStatus, target: FileTarget): List<TrackingRecord> =
        withContext(ioDispatcher) {
            activityFileTrackingDao.getTrackingRecords(status.name, target.name)
                .map {
                    TrackingRecord(
                        it.id,
                        it.activityId,
                        it.activityName,
                        File(it.filePath),
                        FileStatus.valueOf(it.status),
                        FileTarget.valueOf(it.target)
                    )
                }
        }
}
