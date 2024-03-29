package akio.apps.myrun.data.tracking.impl

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface RouteTrackingLocationDao {
    @Query("SELECT * FROM tracking_location ORDER BY time ASC LIMIT -1 OFFSET :skip")
    suspend fun getLocations(skip: Int): List<RoomTrackingLocation>

    @Update
    suspend fun update(updateLocations: List<RoomTrackingLocation>)

    @Insert
    suspend fun insert(trackingLocations: List<RoomTrackingLocation>)

    @Query("DELETE FROM tracking_location")
    suspend fun clear(): Int

    @Query("SELECT * FROM tracking_location")
    suspend fun getAll(): List<RoomTrackingLocation>

    @Query("SELECT time from tracking_location ORDER BY time DESC LIMIT 1")
    suspend fun getLatestLocationTime(): Long
}
