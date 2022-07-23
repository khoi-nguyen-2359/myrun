package akio.apps.myrun.data.tracking

import akio.apps.myrun.data.tracking.impl.RouteTrackingDatabase
import akio.apps.myrun.data.tracking.impl.RouteTrackingLocationDao
import android.app.Application
import androidx.room.Room
import dagger.Module
import dagger.Provides

@Module(includes = [TrackingDataModule.Providers::class])
interface TrackingDataModule {
    @Module
    object Providers {
        @Provides
        @JvmStatic
        fun routeTrackingDatabase(application: Application): RouteTrackingDatabase =
            Room.databaseBuilder(
                application,
                RouteTrackingDatabase::class.java,
                "route_tracking_db"
            )
                .enableMultiInstanceInvalidation()
                .fallbackToDestructiveMigrationFrom(1)
                .build()

        @Provides
        @JvmStatic
        fun routeTrackingLocationDao(database: RouteTrackingDatabase): RouteTrackingLocationDao =
            database.trackingLocationDao()
    }
}
