package akio.apps.myrun.data.activitysharing.impl

import akio.apps.myrun.data.activitysharing.entity.RoomActivityFileRecord
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface ActivityFileDao {
    @Insert
    suspend fun insert(record: RoomActivityFileRecord)

    @Query("SELECT * FROM activity_file WHERE status = :status AND target = :target")
    suspend fun getTrackingRecords(status: String, target: String): List<RoomActivityFileRecord>

    @Query("UPDATE activity_file SET status = :status WHERE id = :recordId")
    suspend fun updateRecord(recordId: Int, status: String)

    @Query("DELETE FROM activity_file WHERE id = :recordId")
    suspend fun delete(recordId: Int)

    @Query("SELECT file_path FROM activity_file WHERE id = :fileId")
    suspend fun getFilePath(fileId: Int): String

    @Query("SELECT count(*) FROM activity_file WHERE status = :status AND target = :target")
    suspend fun count(status: String, target: String): Int
}