package akio.apps.myrun.feature.strava._di

import akio.apps._base.di.ViewModelKey
import akio.apps.myrun.data.externalapp.StravaAuthenticator
import akio.apps.myrun.data.externalapp.StravaTokenStorage
import akio.apps.myrun.data.externalapp._di.ExternalAppDataModule
import akio.apps.myrun.data.externalapp._di.ExternalAppDataModule.Companion.STRAVA_APP_ID
import akio.apps.myrun.data.externalapp._di.ExternalAppDataModule.Companion.STRAVA_APP_SECRET
import akio.apps.myrun.data.externalapp._di.ExternalAppDataModule.Companion.STRAVA_BASE_ENDPOINT
import akio.apps.myrun.feature.editprofile.UpdateStravaTokenUsecase
import akio.apps.myrun.feature.strava.*
import akio.apps.myrun.feature.strava.impl.*
import akio.apps.myrun.feature.userprofile.RemoveStravaTokenUsecase
import androidx.lifecycle.ViewModel
import com.google.gson.Gson
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.android.ContributesAndroidInjector
import dagger.multibindings.IntoMap
import okhttp3.OkHttpClient
import javax.inject.Named

@Module(includes = [StravaConnectFeatureModule.Bindings::class, StravaConnectFeatureModule.AndroidInjectors::class])
class StravaConnectFeatureModule {

    @Module
    interface Bindings {
        @Binds
        fun getStravaRoutesUsecase(usecase: GetStravaRoutesUsecaseImpl): GetStravaRoutesUsecase

        @Binds
        fun updateLoadRunToStravaUsecase(usecase: UploadActivityFilesToStravaUsecaseImpl): UploadActivityFilesToStravaUsecase

        @Binds
        fun exportRunToFileUsecase(exportRunFileUsecaseImpl: ExportTrackingActivityToStravaFileUsecaseImpl): ExportTrackingActivityToStravaFileUsecase

        @Binds
        fun getTcxActivityFileExporter(tcxActivityFileExporter: TcxActivityFileExporter): ActivityFileExporter

        @Binds
        fun getActivityFileManager(managerImpl: ActivityFileManagerImpl): ActivityFileManager

        @Binds
        fun initStravaUploadWorkerUsecase(initStravaUploadWorkerUsecase: InitializeStravaUploadWorkerUsecaseImpl): InitializeStravaUploadWorkerUsecase

        @Binds
        @IntoMap
        @ViewModelKey(LinkStravaViewModel::class)
        fun linkStravaViewModel(viewModelImpl: LinkStravaViewModelImpl): ViewModel
    }

    @Module
    interface AndroidInjectors {
        @ContributesAndroidInjector
        fun uploadStravaFileWorker(): UploadStravaFileWorker
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