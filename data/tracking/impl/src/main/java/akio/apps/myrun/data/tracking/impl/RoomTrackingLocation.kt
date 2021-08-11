package akio.apps.myrun.data.tracking.impl

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tracking_location")
data class RoomTrackingLocation(
    @PrimaryKey(autoGenerate = true) val id: Int,
    val time: Long,
    val latitude: Double,
    val longitude: Double,
    val altitude: Double,
    val speed: Double
)
