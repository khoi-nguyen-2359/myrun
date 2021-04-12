package akio.apps.myrun._di

import android.app.Application
import android.content.Context
import dagger.Module
import dagger.Provides

@Module(includes = [AppModule.Providers::class])
interface AppModule {
    @Module
    class Providers {
        @Provides
        fun applicationContext(application: Application): Context = application
    }
}
