package akio.apps.myrun.data.location

import akio.apps.myrun.data.location.api.DirectionDataSource
import akio.apps.myrun.data.location.impl.GoogleDirectionDataSource
import dagger.Binds
import dagger.Module

@Module
interface DirectionDataModule {
    @Binds
    fun directionDataSource(
        googleDirectionDataSource: GoogleDirectionDataSource,
    ): DirectionDataSource
}
