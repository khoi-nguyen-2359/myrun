package akio.apps.myrun.data.place._di

import akio.apps.myrun.data.place.PlaceDataSource
import akio.apps.myrun.data.place.impl.PlaceDataSourceImpl
import dagger.Binds
import dagger.Module

@Module(includes = [PlaceDataModule.Bindings::class])
class PlaceDataModule {

    @Module
    interface Bindings {
        @Binds
        fun placeDataSource(placeDataSourceImpl: PlaceDataSourceImpl): PlaceDataSource
    }
}