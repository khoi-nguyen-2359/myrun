package akio.apps.myrun.wiring.data.tracking

import akio.apps.myrun.data.tracking.api.RouteTrackingConfiguration
import akio.apps.myrun.data.tracking.api.RouteTrackingLocationRepository
import akio.apps.myrun.data.tracking.api.RouteTrackingState
import akio.apps.myrun.data.tracking.impl.PreferencesRouteTrackingState
import akio.apps.myrun.data.tracking.impl.RouteTrackingConfigurationImpl
import akio.apps.myrun.data.tracking.impl.RouteTrackingDatabase
import akio.apps.myrun.data.tracking.impl.RouteTrackingLocationDao
import akio.apps.myrun.data.tracking.impl.RouteTrackingLocationRepositoryImpl
import android.app.Application
import androidx.room.Room
import dagger.Binds
import dagger.Module
import dagger.Provides

@Module(includes = [TrackingDataModule.Providers::class])
internal interface TrackingDataModule {

    @Binds
    fun routeTrackingLocationRepo(repositoryImpl: RouteTrackingLocationRepositoryImpl):
        RouteTrackingLocationRepository

    @Binds
    fun routeTrackingConfiguration(impl: RouteTrackingConfigurationImpl): RouteTrackingConfiguration

    @Binds
    fun routeTrackingState(impl: PreferencesRouteTrackingState): RouteTrackingState

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
                .fallbackToDestructiveMigrationFrom(1)
                .build()

        @Provides
        fun routeTrackingLocationDao(database: RouteTrackingDatabase): RouteTrackingLocationDao =
            database.trackingLocationDao()
    }
}
