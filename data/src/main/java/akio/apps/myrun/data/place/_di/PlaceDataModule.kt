package akio.apps.myrun.data.place._di

import akio.apps.myrun.data.place.PlaceDataSource
import akio.apps.myrun.data.place.impl.PlaceDataSourceImpl
import android.content.Context
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.net.PlacesClient
import dagger.Binds
import dagger.Module
import dagger.Provides

@Module(includes = [PlaceDataModule.Providers::class])
interface PlaceDataModule {

    @Binds
    fun placeDataSource(placeDataSourceImpl: PlaceDataSourceImpl): PlaceDataSource

    @Module
    class Providers {
        @Provides
        fun placesClient(appContext: Context): PlacesClient = Places.createClient(appContext)
    }
}
