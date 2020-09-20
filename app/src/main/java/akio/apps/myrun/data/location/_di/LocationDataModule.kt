package akio.apps.myrun.data.location._di

import akio.apps.myrun.data.location.LocationDataSource
import akio.apps.myrun.data.location.impl.LocationDataSourceImpl
import android.content.Context
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import dagger.Binds
import dagger.Module
import dagger.Provides

@Module(includes = [LocationDataModule.Bindings::class])
class LocationDataModule {
    @Provides
    fun locationClient(appContext: Context): FusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(appContext)

    @Module
    interface Bindings {
        @Binds
        fun locationDataSource(locationDataSourceImpl: LocationDataSourceImpl): LocationDataSource
    }
}