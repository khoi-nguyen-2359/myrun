package akio.apps.myrun.data.routetracking._di

import akio.apps.myrun.data.routetracking.RouteTrackingLocationRepository
import akio.apps.myrun.data.routetracking.RouteTrackingState
import akio.apps.myrun.data.routetracking.impl.PreferencesRouteTrackingState
import akio.apps.myrun.data.routetracking.impl.RouteTrackingDatabase
import akio.apps.myrun.data.routetracking.impl.RouteTrackingLocationDao
import akio.apps.myrun.data.routetracking.impl.RouteTrackingLocationRepositoryImpl
import android.content.Context
import androidx.room.Room
import dagger.Binds
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module(includes = [RouteTrackingDataModule.Providers::class])
interface RouteTrackingDataModule {

    @Module
    class Providers {
        @Provides
        @Singleton
        fun routeTrackingDatabase(application: Context): RouteTrackingDatabase =
            Room.databaseBuilder(
                application,
                RouteTrackingDatabase::class.java,
                "route_tracking_db"
            )
                .build()

        @Provides
        fun routeTrackingLocationDao(database: RouteTrackingDatabase): RouteTrackingLocationDao =
            database.trackingLocationDao()
    }

    @Binds
    fun routeTrackingLocationRepo(repositoryImpl: RouteTrackingLocationRepositoryImpl): RouteTrackingLocationRepository

    @Binds
    @Singleton
    fun routeTrackingState(preferencesRouteTrackingState: PreferencesRouteTrackingState): RouteTrackingState
}
