package akio.apps.myrun.data.activityfile.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "activity_file")
class ActivityFileRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Int,

    @ColumnInfo(name = "activity_id")
    val activityId: String,

    @ColumnInfo(name = "activity_name")
    val activityName: String,

    @ColumnInfo(name = "file_path")
    val filePath: String,

    @ColumnInfo(name = "status")
    val status: String,

    @ColumnInfo(name = "target")
    val target: String
)