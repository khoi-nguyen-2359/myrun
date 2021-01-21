package akio.apps.myrun.data.activityfile

import akio.apps.myrun.data.activityfile.model.FileStatus
import akio.apps.myrun.data.activityfile.model.FileTarget
import akio.apps.myrun.data.activityfile.model.TrackingRecord
import java.io.File

interface ActivityFileTrackingRepository {
    fun createEmptyFile(activityId: String): File
    suspend fun track(
        activityId: String,
        activityName: String,
        activityFile: File,
        target: FileTarget
    )

    suspend fun updateStatus(fileId: Int, status: FileStatus)
    suspend fun delete(fileId: Int)
    suspend fun getRecords(status: FileStatus, target: FileTarget): List<TrackingRecord>
    suspend fun countRecord(status: FileStatus, target: FileTarget): Int
}
