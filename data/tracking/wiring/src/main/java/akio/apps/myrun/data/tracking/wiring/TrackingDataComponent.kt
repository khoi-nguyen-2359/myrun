package akio.apps.myrun.data.tracking.wiring

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
import akio.apps.myrun.data.wiring.ApplicationModule
import akio.apps.myrun.data.wiring.DispatchersModule
import akio.apps.myrun.data.wiring.FirebaseDataModule
import android.app.Application
import androidx.room.Room
import dagger.Binds
import dagger.Component
import dagger.Module
import dagger.Provides

@Component(
    modules = [
        TrackingDataModule::class,
        ApplicationModule::class,
        DispatchersModule::class,
        FirebaseDataModule::class
    ]
)
interface TrackingDataComponent {
    fun routeTrackingLocationRepo(): RouteTrackingLocationRepository
    fun routeTrackingConfiguration(): RouteTrackingConfiguration
    fun routeTrackingState(): RouteTrackingState
    fun fitnessDataRepository(): FitnessDataRepository
}

@Module(includes = [TrackingDataModule.Providers::class])
internal interface TrackingDataModule {

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
