package akio.apps.myrun.data.routetracking._di

import akio.apps.myrun.data.routetracking.RouteTrackingLocationRepository
import akio.apps.myrun.data.routetracking.RouteTrackingState
import akio.apps.myrun.data.routetracking.impl.PreferencesRouteTrackingState
import akio.apps.myrun.data.routetracking.impl.RouteTrackingDatabase
import akio.apps.myrun.data.routetracking.impl.RouteTrackingLocationDao
import akio.apps.myrun.data.routetracking.impl.RouteTrackingLocationRepositoryImpl
import android.app.Application
import androidx.room.Room
import dagger.Binds
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module(includes = [RouteTrackingDataModule.Providers::class])
interface RouteTrackingDataModule {

    @Binds
    fun routeTrackingLocationRepo(repositoryImpl: RouteTrackingLocationRepositoryImpl):
        RouteTrackingLocationRepository

    @Module
    class Providers {
        @Provides
        fun routeTrackingDatabase(application: Application): RouteTrackingDatabase =
            Room.databaseBuilder(
                application,
                RouteTrackingDatabase::class.java,
                "route_tracking_db"
            )
                .enableMultiInstanceInvalidation()
                .build()

        @Provides
        fun routeTrackingLocationDao(database: RouteTrackingDatabase): RouteTrackingLocationDao =
            database.trackingLocationDao()
    }

    @Module
    interface RouteTrackingStateDataModule {
        @Binds
        fun routeTrackingState(preferencesRouteTrackingState: PreferencesRouteTrackingState):
            RouteTrackingState
    }
}
