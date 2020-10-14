package akio.apps.myrun.data.externalapp._di

import akio.apps.myrun.STRAVA_APP_ID
import akio.apps.myrun.STRAVA_APP_SECRET
import akio.apps.myrun.STRAVA_BASE_ENDPOINT
import akio.apps.myrun.data.authentication.UserAuthenticationState
import akio.apps.myrun.data.externalapp.ExternalAppProvidersRepository
import akio.apps.myrun.data.externalapp.impl.StravaApi
import akio.apps.myrun.data.externalapp.StravaTokenStorage
import akio.apps.myrun.data.externalapp.impl.ExternalAppProvidersRepositoryImpl
import akio.apps.myrun.data.externalapp.impl.StravaTokenStorageImpl
import akio.apps.myrun.data.externalapp.impl.StravaAuthenticator
import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import dagger.Binds
import dagger.Module
import dagger.Provides
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Named
import javax.inject.Singleton

@Module(includes = [ExternalAppDataModule.Bindings::class])
class ExternalAppDataModule {
    @Module
    interface Bindings {
        @Binds
        fun externalAppCredentialsRepo(repo: ExternalAppProvidersRepositoryImpl): ExternalAppProvidersRepository

        @Binds
        fun stravaTokenStorage(storageImpl: StravaTokenStorageImpl): StravaTokenStorage
    }

    @Provides
    @Named(NAME_STRAVA_GSON)
    fun stravaGson(): Gson = Gson()

    @Provides
    fun stravaApiTokenRefreshAuthenticator(
        okHttpClientBuilder: OkHttpClient.Builder,
        externalAppProvidersRepository: ExternalAppProvidersRepository,
        userAuthenticationState: UserAuthenticationState,
        stravaTokenStorage: StravaTokenStorage,
        @Named(NAME_STRAVA_GSON) gson: Gson
    ): StravaAuthenticator {
        val refreshTokenClient = okHttpClientBuilder.build()
        return StravaAuthenticator(
            refreshTokenClient,
            externalAppProvidersRepository,
            userAuthenticationState,
            stravaTokenStorage,
            STRAVA_BASE_ENDPOINT,
            gson,
            STRAVA_APP_ID,
            STRAVA_APP_SECRET
        )
    }

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

    @Provides
    @Named(NAME_STRAVA_TOKEN_PREFERENCES)
    fun stravaTokenPreference(context: Context): SharedPreferences = context.getSharedPreferences(PREFS_STRAVA_TOKEN, Context.MODE_PRIVATE)

    companion object {
        const val PREFS_STRAVA_TOKEN = "ExternalAppDataModule.PREFS_STRAVA_TOKEN"

        const val NAME_STRAVA_GSON = "ExternalAppDataModule.NAME_STRAVA_GSON"
        const val NAME_STRAVA_TOKEN_PREFERENCES = "ExternalAppDataModule.NAME_STRAVA_TOKEN_PREFERENCES"
    }
}