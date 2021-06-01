package akio.apps.myrun.data.activitysharing.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "export_activity_location")
data class RoomExportActivityLocation(
    @PrimaryKey(autoGenerate = true) val id: Int,
    val activityId: String,
    val time: Long,
    val latitude: Double,
    val longitude: Double,
    val altitude: Double
)
