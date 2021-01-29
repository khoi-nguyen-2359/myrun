package akio.apps.myrun.data.activityfile.impl

import akio.apps.myrun.data.activityfile.entity.ActivityFileRecord
import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [ActivityFileRecord::class], version = 1)
abstract class ActivityFileTrackingDatabase : RoomDatabase() {
    abstract fun activityFileDao(): ActivityFileDao
}
