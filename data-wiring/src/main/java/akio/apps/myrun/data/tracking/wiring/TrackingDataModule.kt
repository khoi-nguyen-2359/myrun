package akio.apps.myrun.data.tracking.wiring

import akio.apps.myrun.data.location.LocationDataSource
import akio.apps.myrun.data.location.impl.LocationDataSourceImpl
import akio.apps.myrun.data.place.PlaceDataSource
import akio.apps.myrun.data.place.impl.GooglePlaceDataSource
import akio.apps.myrun.data.routetracking.RouteTrackingConfiguration
import akio.apps.myrun.data.routetracking.RouteTrackingLocationRepository
import akio.apps.myrun.data.routetracking.RouteTrackingState
import akio.apps.myrun.data.routetracking.impl.PreferencesRouteTrackingState
import akio.apps.myrun.data.routetracking.impl.RouteTrackingConfigurationImpl
import akio.apps.myrun.data.routetracking.impl.RouteTrackingDatabase
import akio.apps.myrun.data.routetracking.impl.RouteTrackingLocationDao
import akio.apps.myrun.data.routetracking.impl.RouteTrackingLocationRepositoryImpl
import android.app.Application
import androidx.room.Room
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.net.PlacesClient
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

    @Binds
    fun locationDataSource(locationDataSourceImpl: LocationDataSourceImpl): LocationDataSource

    @Binds
    fun placeDataSource(googlePlaceDataSource: GooglePlaceDataSource): PlaceDataSource

    @Module
    class Providers {
        @Provides
        fun placesClient(application: Application): PlacesClient = Places.createClient(application)

        @Provides
        fun locationClient(application: Application): FusedLocationProviderClient =
            LocationServices.getFusedLocationProviderClient(application)

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
