package akio.apps.myrun._di

import akio.apps.myrun.MyRunApp
import android.content.Context
import dagger.Module
import dagger.Provides

@Module(includes = [AppModule.Providers::class])
interface AppModule {
    @Module
    class Providers {
        @Provides
        fun applicationContext(application: MyRunApp): Context = application
    }
}
