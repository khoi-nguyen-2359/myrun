package akio.apps.myrun.data.activityexport

import akio.apps.myrun.data.activityexport.model.FileStatus
import akio.apps.myrun.data.activityexport.model.FileTarget
import akio.apps.myrun.data.activityexport.model.TrackingRecord
import java.io.File

interface ActivityFileTrackingRepository {
    fun createEmptyFile(activityId: String): File
    suspend fun track(
        activityId: String,
        activityName: String,
        activityFile: File,
        target: FileTarget,
        initStatus: FileStatus = FileStatus.PENDING
    ): TrackingRecord

    suspend fun updateStatus(fileId: Int, status: FileStatus)
    suspend fun delete(fileId: Int)
    suspend fun getRecords(status: FileStatus, target: FileTarget): List<TrackingRecord>
    suspend fun countRecord(status: FileStatus, target: FileTarget): Int
}
