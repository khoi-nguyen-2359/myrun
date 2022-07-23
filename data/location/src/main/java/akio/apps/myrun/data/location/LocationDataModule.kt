package akio.apps.myrun.data.location

import akio.apps.myrun.data.location.impl.GoogleMapDirectionApi
import akio.apps.myrun.data.location.impl.model.GoogleMapDirectionApiKey
import android.app.Application
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.gson.Gson
import dagger.Module
import dagger.Provides
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

@Module(
    includes = [
        LocationDataModule.Providers::class,
        DirectionDataModule::class,
    ]
)
interface LocationDataModule {
    @Module
    object Providers {
        @Provides
        @JvmStatic
        fun placesClient(application: Application): PlacesClient = Places.createClient(application)

        @Provides
        @JvmStatic
        fun locationClient(application: Application): FusedLocationProviderClient =
            LocationServices.getFusedLocationProviderClient(application)

        @Provides
        @JvmStatic
        fun googleDirectionApi(): GoogleMapDirectionApi {
            val okHttpClientBuilder = OkHttpClient.Builder()
            if (BuildConfig.DEBUG) {
                val logger = HttpLoggingInterceptor().apply {
                    level = HttpLoggingInterceptor.Level.BODY
                }
                okHttpClientBuilder.addInterceptor(logger)
            }
            val okHttpClient = okHttpClientBuilder.build()

            val retrofit = Retrofit.Builder()
                .client(okHttpClient)
                .baseUrl("https://maps.googleapis.com/")
                .addConverterFactory(GsonConverterFactory.create(Gson()))
                .build()

            return retrofit.create(GoogleMapDirectionApi::class.java)
        }

        @Provides
        @JvmStatic
        fun googleDirectionApiKey(application: Application): GoogleMapDirectionApiKey =
            GoogleMapDirectionApiKey(application.getString(R.string.google_direction_api_key))
    }
}
