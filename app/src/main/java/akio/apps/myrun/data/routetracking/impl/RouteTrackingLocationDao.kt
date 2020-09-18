package akio.apps.myrun.data.routetracking.impl

import akio.apps.myrun.data.routetracking.dto.RoomTrackingLocation
import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface RouteTrackingLocationDao {
    @Query("SELECT * FROM tracking_location ORDER BY time ASC LIMIT -1 OFFSET :skip")
    fun getLocations(skip: Int): Flow<List<RoomTrackingLocation>>

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