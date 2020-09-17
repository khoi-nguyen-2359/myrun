package akio.apps.myrun.data.routetracking.impl

import akio.apps.myrun.data.routetracking.RouteTrackingLocationRepository
import akio.apps.myrun.data.routetracking.RouteTrackingState
import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class RouteTrackingDataModule {

    @Provides
    @Singleton
    fun routeTrackingDatabase(application: Context): RouteTrackingDatabase =
        Room.databaseBuilder(application, RouteTrackingDatabase::class.java, "route_tracking_db").build()

    @Provides
    fun routeTrackingLocationDao(database: RouteTrackingDatabase): RouteTrackingLocationDao = database.trackingLocationDao()

    @Provides
    fun routeTrackingLocationRepo(repositoryImpl: RouteTrackingLocationRepositoryImpl): RouteTrackingLocationRepository = repositoryImpl

    @Provides
    fun routeTrackingState(routeTrackingStateImpl: RouteTrackingStateImpl): RouteTrackingState = routeTrackingStateImpl
}