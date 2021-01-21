package akio.apps.myrun._di

import akio.apps.myrun.R
import android.content.Context
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.net.PlacesClient
import dagger.Module
import dagger.Provides
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Named
import javax.inject.Singleton

@Module
class GoogleMapApiModule {

    @Provides
    fun placesClient(appContext: Context): PlacesClient = Places.createClient(appContext)

    @Provides
    @Singleton
    @Named(NAME_GOOGLE_MAP_API_RETROFIT)
    fun googleMapApiRetrofit(okHttpClientBuilder: OkHttpClient.Builder): Retrofit {
        val okHttpClient = okHttpClientBuilder.build()
        return Retrofit.Builder()
            .client(okHttpClient)
            .baseUrl("https://maps.googleapis.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Named(NAME_GOOGLE_MAP_API_KEY)
    fun googleMapApiKey(appContext: Context) = appContext.getString(R.string.google_maps_sdk_key)

    companion object {
        const val NAME_GOOGLE_MAP_API_RETROFIT = "GoogleMapApiModule.NAMED_GOOGLE_MAP_API_RETROFIT"
        const val NAME_GOOGLE_MAP_API_KEY = "GoogleMapApiModule.NAME_GOOGLE_MAP_API_KEY"
    }
}
