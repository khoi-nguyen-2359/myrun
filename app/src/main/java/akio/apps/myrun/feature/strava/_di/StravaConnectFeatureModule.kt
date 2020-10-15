package akio.apps.myrun.feature.strava._di

import akio.apps.myrun.data.externalapp.StravaAuthenticator
import akio.apps.myrun.data.externalapp.StravaTokenStorage
import akio.apps.myrun.data.externalapp._di.ExternalAppDataModule
import akio.apps.myrun.data.externalapp._di.ExternalAppDataModule.Companion.STRAVA_APP_ID
import akio.apps.myrun.data.externalapp._di.ExternalAppDataModule.Companion.STRAVA_APP_SECRET
import akio.apps.myrun.data.externalapp._di.ExternalAppDataModule.Companion.STRAVA_BASE_ENDPOINT
import akio.apps.myrun.feature.strava.impl.StravaAuthenticatorImpl
import akio.apps.myrun.feature.editprofile.UpdateStravaTokenUsecase
import akio.apps.myrun.feature.strava.ExportRunToFileUsecase
import akio.apps.myrun.feature.strava.GetStravaRoutesUsecase
import akio.apps.myrun.feature.strava.UploadActivityToStravaUsecase
import akio.apps.myrun.feature.strava.impl.ExportRunToFileUsecaseImpl
import akio.apps.myrun.feature.strava.impl.GetStravaRoutesUsecaseImpl
import akio.apps.myrun.feature.strava.impl.UploadActivityToStravaUsecaseImpl
import akio.apps.myrun.feature.userprofile.RemoveStravaTokenUsecase
import com.google.gson.Gson
import dagger.Binds
import dagger.Module
import dagger.Provides
import okhttp3.OkHttpClient
import javax.inject.Named

@Module(includes = [StravaConnectFeatureModule.Bindings::class])
class StravaConnectFeatureModule {

    @Module
    interface Bindings {
        @Binds
        fun getStravaRoutesUsecase(usecase: GetStravaRoutesUsecaseImpl): GetStravaRoutesUsecase

        @Binds
        fun updateLoadRunToStravaUsecase(usecase: UploadActivityToStravaUsecaseImpl): UploadActivityToStravaUsecase

        @Binds
        fun exportRunToFileUsecase(exportRunToFileUsecase: ExportRunToFileUsecaseImpl): ExportRunToFileUsecase
    }

    @Provides
    fun stravaApiTokenRefreshAuthenticator(
        okHttpClientBuilder: OkHttpClient.Builder,
        updateStravaTokenUsecase: UpdateStravaTokenUsecase,
        removeStravaTokenUsecase: RemoveStravaTokenUsecase,
        stravaTokenStorage: StravaTokenStorage,
        @Named(ExternalAppDataModule.NAME_STRAVA_GSON) gson: Gson
    ): StravaAuthenticator {
        val refreshTokenClient = okHttpClientBuilder.build()
        return StravaAuthenticatorImpl(
            refreshTokenClient,
            updateStravaTokenUsecase,
            removeStravaTokenUsecase,
            stravaTokenStorage,
            STRAVA_BASE_ENDPOINT,
            gson,
            STRAVA_APP_ID,
            STRAVA_APP_SECRET
        )
    }
}