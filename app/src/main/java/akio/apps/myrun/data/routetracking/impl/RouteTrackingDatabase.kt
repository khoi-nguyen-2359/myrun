package akio.apps.myrun.data.routetracking.impl

import akio.apps.myrun.data.routetracking.model.RoomTrackingLocation
import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [RoomTrackingLocation::class], version = 1)
abstract class RouteTrackingDatabase: RoomDatabase() {
    abstract fun trackingLocationDao(): RouteTrackingLocationDao
}