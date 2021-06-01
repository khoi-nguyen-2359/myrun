package akio.apps.myrun.data.activitysharing.impl

import akio.apps.myrun.data.activitysharing.entity.RoomActivityFileRecord
import akio.apps.myrun.data.activitysharing.entity.RoomExportActivityLocation
import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [RoomActivityFileRecord::class, RoomExportActivityLocation::class],
    version = 1
)
abstract class ActivitySharingDatabase : RoomDatabase() {
    abstract fun activityFileDao(): ActivityFileDao
    abstract fun activityLocationDao(): ActivityLocationCacheDao
}
