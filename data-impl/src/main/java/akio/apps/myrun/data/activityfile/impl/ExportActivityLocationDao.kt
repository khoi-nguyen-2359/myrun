package akio.apps.myrun.data.activityfile.impl

import akio.apps.myrun.data.activityfile.entity.RoomExportActivityLocation
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface ExportActivityLocationDao {
    @Insert
    suspend fun insert(entities: List<RoomExportActivityLocation>)

    @Query("SELECT * FROM export_activity_location WHERE activityId = :activityId")
    suspend fun getActivityLocations(activityId: String): List<RoomExportActivityLocation>

    @Query("DELETE FROM export_activity_location WHERE activityId = :activityId")
    fun delete(activityId: String)
}
