package akio.apps.myrun.data.place._di

import akio.apps.myrun.data.place.PlaceDataSource
import akio.apps.myrun.data.place.impl.GooglePlaceDataSource
import android.app.Application
import android.content.Context
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.net.PlacesClient
import dagger.Binds
import dagger.Module
import dagger.Provides

@Module(includes = [PlaceDataModule.Providers::class])
interface PlaceDataModule {

    @Binds
    fun placeDataSource(googlePlaceDataSource: GooglePlaceDataSource): PlaceDataSource

    @Module
    class Providers {
        @Provides
        fun placesClient(application: Application): PlacesClient = Places.createClient(application)
    }
}
