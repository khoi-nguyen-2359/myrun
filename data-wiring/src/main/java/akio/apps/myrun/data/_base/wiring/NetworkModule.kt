package akio.apps.myrun.data._base.wiring

import akio.apps.myrun.data.BuildConfig
import dagger.Module
import dagger.Provides
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor

@Module
object NetworkModule {
    @Provides
    @JvmStatic
    fun okHttpClient(): OkHttpClient.Builder {
        val builder = OkHttpClient.Builder()
        if (BuildConfig.DEBUG) {
            val logger = HttpLoggingInterceptor()
            logger.level = HttpLoggingInterceptor.Level.BODY
            builder.addInterceptor(logger)
        }

        return builder
    }
}
