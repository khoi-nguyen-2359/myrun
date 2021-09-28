package akio.apps.myrun.data.location.wiring

import akio.apps.myrun.data.location.api.DirectionDataSource
import akio.apps.myrun.data.location.api.LocationDataSource
import akio.apps.myrun.data.location.api.PlaceDataSource
import akio.apps.myrun.data.location.impl.GoogleDirectionDataSource
import akio.apps.myrun.data.location.impl.GoogleMapDirectionApi
import akio.apps.myrun.data.location.impl.GooglePlaceDataSource
import akio.apps.myrun.data.location.impl.LocationDataSourceImpl
import akio.apps.myrun.data.location.impl.model.GoogleMapApiKey
import android.app.Application
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.gson.Gson
import dagger.Binds
import dagger.Module
import dagger.Provides
import javax.inject.Singleton
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

@Module(includes = [LocationDataModule.Providers::class])
internal interface LocationDataModule {
    @Binds
    fun locationDataSource(locationDataSourceImpl: LocationDataSourceImpl): LocationDataSource

    @Binds
    fun placeDataSource(googlePlaceDataSource: GooglePlaceDataSource): PlaceDataSource

    @Binds
    fun directionDataSource(
        googleDirectionDataSource: GoogleDirectionDataSource,
    ): DirectionDataSource

    @Module
    class Providers {
        @Provides
        fun placesClient(application: Application): PlacesClient = Places.createClient(application)

        @Provides
        fun locationClient(application: Application): FusedLocationProviderClient =
            LocationServices.getFusedLocationProviderClient(application)

        @Provides
        @Singleton
        fun googleDirectionApi(okHttpClientBuilder: OkHttpClient.Builder): GoogleMapDirectionApi {
            val okHttpClient = okHttpClientBuilder.build()

            val retrofit = Retrofit.Builder()
                .client(okHttpClient)
                .baseUrl("https://maps.googleapis.com/")
                .addConverterFactory(GsonConverterFactory.create(Gson()))
                .build()

            return retrofit.create(GoogleMapDirectionApi::class.java)
        }

        @Provides
        fun googleMapApiKey(application: Application): GoogleMapApiKey =
            GoogleMapApiKey(value = application.getString(R.string.google_api_key))
    }
}
