package akio.apps.myrun.data.activityfile.impl

import akio.apps.myrun.data.activityfile.entity.ActivityFileRecord
import akio.apps.myrun.data.activityfile.entity.RoomExportActivityLocation
import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [ActivityFileRecord::class, RoomExportActivityLocation::class], version = 1)
abstract class ActivityFileDatabase : RoomDatabase() {
    abstract fun activityFileDao(): ActivityFileDao
    abstract fun activityLocationDao(): ExportActivityLocationDao
}
