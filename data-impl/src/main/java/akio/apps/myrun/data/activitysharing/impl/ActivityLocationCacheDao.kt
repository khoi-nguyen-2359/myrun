package akio.apps.myrun.data.activitysharing.impl

import akio.apps.myrun.data.activitysharing.entity.RoomExportActivityLocation
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface ActivityLocationCacheDao {
    @Insert
    suspend fun insert(entities: List<RoomExportActivityLocation>)

    @Query("SELECT * FROM export_activity_location WHERE activityId = :activityId")
    suspend fun getActivityLocations(activityId: String): List<RoomExportActivityLocation>

    @Query("DELETE FROM export_activity_location WHERE activityId = :activityId")
    fun delete(activityId: String)
}
