package akio.apps.myrun.data.activitysharing.impl

import akio.apps.myrun._di.NamedIoDispatcher
import akio.apps.myrun.data.activitysharing.ActivityFileTrackingRepository
import akio.apps.myrun.data.activitysharing.entity.RoomActivityFileRecord
import akio.apps.myrun.data.activitysharing.model.FileStatus
import akio.apps.myrun.data.activitysharing.model.FileTarget
import akio.apps.myrun.data.activitysharing.model.TrackingRecord
import android.app.Application
import java.io.File
import javax.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

class ActivityFileTrackingRepositoryImpl @Inject constructor(
    application: Application,
    activitySharingDatabase: ActivitySharingDatabase,
    @NamedIoDispatcher private val ioDispatcher: CoroutineDispatcher
) : ActivityFileTrackingRepository {

    private val contentDir: File = File(application.filesDir, "activity/")

    private val activityFilePrefix = "activity_"

    private val activityFileTrackingDao = activitySharingDatabase.activityFileDao()

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
        target: FileTarget,
        initStatus: FileStatus
    ): TrackingRecord = withContext(ioDispatcher) {
        val record = RoomActivityFileRecord(
            id = 0,
            activityId = activityId,
            activityName = activityName,
            filePath = activityFile.absolutePath,
            status = FileStatus.PENDING.name,
            target = target.name
        )
        activityFileTrackingDao.insert(record)
        record.toTrackingRecord()
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
                .map { it.toTrackingRecord() }
        }

    private fun RoomActivityFileRecord.toTrackingRecord() = TrackingRecord(
        id,
        activityId,
        activityName,
        File(filePath),
        FileStatus.valueOf(status),
        FileTarget.valueOf(target)
    )
}
