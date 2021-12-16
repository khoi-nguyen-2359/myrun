package akio.apps.myrun.data.tracking.impl

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [RoomTrackingLocation::class], version = 2)
abstract class RouteTrackingDatabase : RoomDatabase() {
    abstract fun trackingLocationDao(): RouteTrackingLocationDao
}
