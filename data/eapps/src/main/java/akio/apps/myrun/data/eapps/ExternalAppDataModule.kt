package akio.apps.myrun.data.eapps

import akio.apps.myrun.data.authentication.api.UserAuthenticationState
import akio.apps.myrun.data.eapps.api.ExternalAppProvidersRepository
import akio.apps.myrun.data.eapps.api.StravaDataRepository
import akio.apps.myrun.data.eapps.api.StravaTokenRepository
import akio.apps.myrun.data.eapps.impl.FirebaseExternalAppProvidersRepository
import akio.apps.myrun.data.eapps.impl.StravaApi
import akio.apps.myrun.data.eapps.impl.StravaAuthenticator
import akio.apps.myrun.data.eapps.impl.StravaDataRepositoryImpl
import akio.apps.myrun.data.eapps.impl.StravaTokenRepositoryImpl
import akio.apps.myrun.data.eapps.impl.model.StravaTokenRefreshMapper
import akio.apps.myrun.wiring.common.NetworkModule
import com.google.gson.Gson
import dagger.Binds
import dagger.Module
import dagger.Provides
import javax.inject.Named
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

@Module(includes = [ExternalAppDataModule.Providers::class, NetworkModule::class])
interface ExternalAppDataModule {
    @Binds
    fun externalAppCredentialsRepo(repo: FirebaseExternalAppProvidersRepository):
        ExternalAppProvidersRepository

    @Binds
    fun stravaTokenRepository(repositoryImpl: StravaTokenRepositoryImpl): StravaTokenRepository

    @Binds
    fun stravaDataRepository(repositoryImpl: StravaDataRepositoryImpl): StravaDataRepository

    @Module
    object Providers {
        @Provides
        @JvmStatic
        @Named(NAME_STRAVA_GSON)
        fun stravaGson(): Gson = Gson()

        @Provides
        @JvmStatic
        fun stravaApiTokenRefreshAuthenticator(
            okHttpClientBuilder: OkHttpClient.Builder,
            @Named(NAME_STRAVA_GSON) gson: Gson,
            tokenRefreshMapper: StravaTokenRefreshMapper,
            externalAppProvidersRepository: ExternalAppProvidersRepository,
            userAuthenticationState: UserAuthenticationState,
        ): StravaAuthenticator {
            val refreshTokenClient = okHttpClientBuilder.build()
            return StravaAuthenticator(
                refreshTokenClient,
                externalAppProvidersRepository,
                tokenRefreshMapper,
                userAuthenticationState,
                STRAVA_BASE_ENDPOINT,
                gson,
                STRAVA_APP_ID,
                STRAVA_APP_SECRET
            )
        }

        @Provides
        @JvmStatic
        fun stravaApiService(
            okHttpClientBuilder: OkHttpClient.Builder,
            stravaAuthenticator: StravaAuthenticator,
            @Named(NAME_STRAVA_GSON) gson: Gson,
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
