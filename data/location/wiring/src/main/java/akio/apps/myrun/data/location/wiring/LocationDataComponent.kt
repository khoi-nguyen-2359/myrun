package akio.apps.myrun.data.location.wiring

import akio.apps.myrun.data.location.api.DirectionDataSource
import akio.apps.myrun.data.location.api.LocationDataSource
import akio.apps.myrun.data.location.api.PlaceDataSource
import akio.apps.myrun.data.location.api.PolyUtil
import akio.apps.myrun.data.location.api.SphericalUtil
import akio.apps.myrun.data.location.impl.GoogleMapDirectionApi
import akio.apps.myrun.data.location.impl.GooglePlaceDataSource
import akio.apps.myrun.data.location.impl.LocationDataSourceImpl
import akio.apps.myrun.data.location.impl.MapBoxDirectionDataSource
import akio.apps.myrun.data.location.impl.PolyUtilImpl
import akio.apps.myrun.data.location.impl.SphericalUtilImpl
import akio.apps.myrun.data.location.impl.model.GoogleMapDirectionApiKey
import akio.apps.myrun.data.location.impl.model.MapBoxAccessToken
import akio.apps.myrun.data.wiring.ApplicationModule
import akio.apps.myrun.data.wiring.DispatchersModule
import akio.apps.myrun.data.wiring.NetworkModule
import android.app.Application
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.gson.Gson
import dagger.Binds
import dagger.Component
import dagger.Module
import dagger.Provides
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

@Component(
    modules = [
        LocationDataModule::class,
        ApplicationModule::class,
        DispatchersModule::class
    ]
)
interface LocationDataComponent {
    fun placeDataSource(): PlaceDataSource
    fun locationDataSource(): LocationDataSource
    fun directionDataSource(): DirectionDataSource
    fun polyUtil(): PolyUtil
    fun sphericalUtil(): SphericalUtil
}

@Module(includes = [LocationDataModule.Providers::class, NetworkModule::class])
internal interface LocationDataModule {
    @Binds
    fun locationDataSource(locationDataSourceImpl: LocationDataSourceImpl): LocationDataSource

    @Binds
    fun placeDataSource(googlePlaceDataSource: GooglePlaceDataSource): PlaceDataSource

    @Binds
    fun directionDataSource(
        mapBoxDirectionDataSource: MapBoxDirectionDataSource,
    ): DirectionDataSource

    @Binds
    fun sphericalUtil(sphericalUtil: SphericalUtilImpl): SphericalUtil

    @Binds
    fun polyUtil(polyUtil: PolyUtilImpl): PolyUtil

    @Module
    class Providers {
        @Provides
        fun placesClient(application: Application): PlacesClient = Places.createClient(application)

        @Provides
        fun locationClient(application: Application): FusedLocationProviderClient =
            LocationServices.getFusedLocationProviderClient(application)

        @Provides
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
        fun googleDirectionApiKey(application: Application): GoogleMapDirectionApiKey =
            GoogleMapDirectionApiKey(application.getString(R.string.google_direction_api_key))

        @Provides
        fun mapBoxAccessToken(application: Application): MapBoxAccessToken =
            MapBoxAccessToken(application.getString(R.string.mapbox_access_token))
    }
}
