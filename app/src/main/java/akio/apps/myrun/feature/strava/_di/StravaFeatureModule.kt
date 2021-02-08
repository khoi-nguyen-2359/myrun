package akio.apps.myrun.feature.strava._di

import akio.apps._base.di.ViewModelFactoryModule
import akio.apps._base.di.ViewModelKey
import akio.apps.myrun.data.externalapp.StravaAuthenticator
import akio.apps.myrun.data.externalapp.StravaTokenStorage
import akio.apps.myrun.data.externalapp._di.ExternalAppDataModule
import akio.apps.myrun.data.externalapp.entity.StravaTokenRefreshEntityMapper
import akio.apps.myrun.domain.strava.UpdateStravaTokenUsecase
import akio.apps.myrun.domain.strava.RemoveStravaTokenUsecase
import akio.apps.myrun.feature.strava.LinkStravaViewModel
import akio.apps.myrun.feature.strava.impl.LinkStravaActivity
import akio.apps.myrun.feature.strava.impl.LinkStravaViewModelImpl
import akio.apps.myrun.feature.strava.impl.StravaAuthenticatorImpl
import androidx.lifecycle.ViewModel
import com.google.gson.Gson
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.android.ContributesAndroidInjector
import dagger.multibindings.IntoMap
import okhttp3.OkHttpClient
import javax.inject.Named

@Module(includes = [StravaFeatureModule.Providers::class])
interface StravaFeatureModule {
    @ContributesAndroidInjector(
        modules = [
            Bindings::class,
            ViewModelFactoryModule::class
        ]
    )
    fun linkStravaActivity(): LinkStravaActivity

    @Module
    interface Bindings {
        @Binds
        @IntoMap
        @ViewModelKey(LinkStravaViewModel::class)
        fun linkStravaViewModel(viewModelImpl: LinkStravaViewModelImpl): ViewModel
    }

    @Module
    class Providers {
        @Provides
        fun stravaApiTokenRefreshAuthenticator(
            okHttpClientBuilder: OkHttpClient.Builder,
            updateStravaTokenUsecase: UpdateStravaTokenUsecase,
            removeStravaTokenUsecase: RemoveStravaTokenUsecase,
            stravaTokenStorage: StravaTokenStorage,
            @Named(ExternalAppDataModule.NAME_STRAVA_GSON) gson: Gson,
            tokenRefreshMapper: StravaTokenRefreshEntityMapper
        ): StravaAuthenticator {
            val refreshTokenClient = okHttpClientBuilder.build()
            return StravaAuthenticatorImpl(
                refreshTokenClient,
                updateStravaTokenUsecase,
                removeStravaTokenUsecase,
                stravaTokenStorage,
                tokenRefreshMapper,
                ExternalAppDataModule.STRAVA_BASE_ENDPOINT,
                gson,
                ExternalAppDataModule.STRAVA_APP_ID,
                ExternalAppDataModule.STRAVA_APP_SECRET
            )
        }
    }
}
