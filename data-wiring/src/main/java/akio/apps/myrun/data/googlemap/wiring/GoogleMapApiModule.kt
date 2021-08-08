package akio.apps.myrun.data.googlemap.wiring

import akio.apps.myrun.data._base.wiring.NetworkModule
import dagger.Module
import dagger.Provides
import javax.inject.Named
import javax.inject.Singleton
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

@Module(includes = [NetworkModule::class])
class GoogleMapApiModule {

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

    companion object {
        const val NAME_GOOGLE_MAP_API_RETROFIT = "GoogleMapApiModule.NAMED_GOOGLE_MAP_API_RETROFIT"
    }
}
