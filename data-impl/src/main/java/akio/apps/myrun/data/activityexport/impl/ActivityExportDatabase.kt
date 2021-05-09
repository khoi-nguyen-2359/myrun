package akio.apps.myrun.data.activityexport.impl

import akio.apps.myrun.data.activityexport.entity.RoomActivityFileRecord
import akio.apps.myrun.data.activityexport.entity.RoomExportActivityLocation
import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [RoomActivityFileRecord::class, RoomExportActivityLocation::class], version = 1)
abstract class ActivityExportDatabase : RoomDatabase() {
    abstract fun activityFileDao(): ActivityFileDao
    abstract fun activityLocationDao(): ExportActivityLocationDao
}
