package akio.apps.myrun._di

import akio.apps._base.di.ViewModelFactory
import akio.apps.myrun.MyRunApp
import akio.apps.myrun.feature.strava.InitializeStravaUploadWorkerDelegate
import akio.apps.myrun.feature.strava.impl.InitializeStravaUploadWorkerDelegateImpl
import android.content.Context
import androidx.lifecycle.ViewModelProvider
import dagger.Binds
import dagger.Module
import dagger.Provides

@Module(includes = [AppModule.Providers::class])
interface AppModule {

    @Binds
    fun bindInitStravaUploadWorkerDelegate(delegateImpl: InitializeStravaUploadWorkerDelegateImpl): InitializeStravaUploadWorkerDelegate

    @Module
    class Providers {
        @Provides
        fun viewModelFactory(viewModelFactory: ViewModelFactory): ViewModelProvider.Factory =
            viewModelFactory

        @Provides
        fun applicationContext(application: MyRunApp): Context = application
    }

}
