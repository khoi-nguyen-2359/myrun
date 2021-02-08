package akio.apps.myrun.data.externalapp._di

import akio.apps.myrun.data._base.FirebaseDataModule
import akio.apps.myrun.data.externalapp.ExternalAppProvidersRepository
import akio.apps.myrun.data.externalapp.StravaAuthenticator
import akio.apps.myrun.data.externalapp.StravaDataRepository
import akio.apps.myrun.data.externalapp.StravaTokenRepository
import akio.apps.myrun.data.externalapp.StravaTokenStorage
import akio.apps.myrun.data.externalapp.impl.ExternalAppProvidersRepositoryImpl
import akio.apps.myrun.data.externalapp.impl.StravaApi
import akio.apps.myrun.data.externalapp.impl.StravaDataRepositoryImpl
import akio.apps.myrun.data.externalapp.impl.StravaTokenRepositoryImpl
import akio.apps.myrun.data.externalapp.impl.StravaTokenStorageImpl
import com.google.gson.Gson
import dagger.Binds
import dagger.Module
import dagger.Provides
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Named
import javax.inject.Singleton

@Module(includes = [ExternalAppDataModule.Providers::class, FirebaseDataModule::class])
interface ExternalAppDataModule {
    @Binds
    fun externalAppCredentialsRepo(repo: ExternalAppProvidersRepositoryImpl): ExternalAppProvidersRepository

    @Binds
    fun stravaTokenStorage(storageImpl: StravaTokenStorageImpl): StravaTokenStorage

    @Binds
    fun stravaTokenRepository(repositoryImpl: StravaTokenRepositoryImpl): StravaTokenRepository

    @Binds
    fun stravaDataRepository(repositoryImpl: StravaDataRepositoryImpl): StravaDataRepository

    @Module
    class Providers {
        @Provides
        @Named(NAME_STRAVA_GSON)
        fun stravaGson(): Gson = Gson()

        @Provides
        @Singleton
        fun stravaApiService(
            okHttpClientBuilder: OkHttpClient.Builder,
            stravaAuthenticator: StravaAuthenticator,
            @Named(NAME_STRAVA_GSON) gson: Gson
        ): StravaApi {
            okHttpClientBuilder.authenticator(stravaAuthenticator)

            val okHttpClient = okHttpClientBuilder.build()

            val retrofit = Retrofit.Builder()
                .client(okHttpClient)
                .baseUrl(STRAVA_BASE_ENDPOINT)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build()

            return retrofit.create(StravaApi::class.java)
        }
    }

    companion object {
        const val STRAVA_APP_ID = "54817"
        const val STRAVA_APP_SECRET = "805c1da4993b9439d583d4264809b50270ebae3a"
        const val STRAVA_BASE_ENDPOINT = "https://www.strava.com/"

        const val NAME_STRAVA_GSON = "ExternalAppDataModule.NAME_STRAVA_GSON"
    }
}
