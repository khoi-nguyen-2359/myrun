package akio.apps.myrun.wiring.data.location

import akio.apps.myrun.data.location.api.LocationDataSource
import akio.apps.myrun.data.location.api.PlaceDataSource
import akio.apps.myrun.data.location.impl.GooglePlaceDataSource
import akio.apps.myrun.data.location.impl.LocationDataSourceImpl
import android.app.Application
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.net.PlacesClient
import dagger.Binds
import dagger.Module
import dagger.Provides

@Module(includes = [LocationDataModule.Providers::class])
interface LocationDataModule {
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
    }
}
