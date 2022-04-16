package akio.apps.myrun.data.location

import akio.apps.myrun.data.location.api.DirectionDataSource
import akio.apps.myrun.data.location.impl.mapbox.MapBoxAccessToken
import akio.apps.myrun.data.location.impl.mapbox.MapBoxDirectionDataSource
import android.app.Application
import dagger.Binds
import dagger.Module
import dagger.Provides

@Module(includes = [DirectionDataModule.Providers::class])
interface DirectionDataModule {
    @Binds
    fun directionDataSource(impl: MapBoxDirectionDataSource): DirectionDataSource

    @Module
    object Providers {
        @Provides
        @JvmStatic
        fun mapBoxAccessToken(application: Application): MapBoxAccessToken =
            MapBoxAccessToken(application.getString(R.string.mapbox_access_token))
    }
}
