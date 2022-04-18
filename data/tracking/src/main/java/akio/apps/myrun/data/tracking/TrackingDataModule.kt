package akio.apps.myrun.data.tracking

import akio.apps.myrun.data.tracking.api.FitnessDataRepository
import akio.apps.myrun.data.tracking.api.RouteTrackingConfiguration
import akio.apps.myrun.data.tracking.api.RouteTrackingLocationRepository
import akio.apps.myrun.data.tracking.api.RouteTrackingState
import akio.apps.myrun.data.tracking.impl.GoogleFitnessDataRepository
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
interface TrackingDataModule {
    @Binds
    fun routeTrackingLocationRepo(repositoryImpl: RouteTrackingLocationRepositoryImpl):
        RouteTrackingLocationRepository

    @Binds
    fun routeTrackingConfiguration(impl: RouteTrackingConfigurationImpl): RouteTrackingConfiguration

    @Binds
    fun routeTrackingState(impl: PreferencesRouteTrackingState): RouteTrackingState

    @Binds
    fun fitnessDataRepository(repositoryGoogle: GoogleFitnessDataRepository): FitnessDataRepository

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
