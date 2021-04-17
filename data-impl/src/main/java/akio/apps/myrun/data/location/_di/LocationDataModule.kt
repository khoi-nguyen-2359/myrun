package akio.apps.myrun.data.location._di

import akio.apps.myrun.data.location.LocationDataSource
import akio.apps.myrun.data.location.impl.LocationDataSourceImpl
import android.app.Application
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import dagger.Binds
import dagger.Module
import dagger.Provides

@Module(includes = [LocationDataModule.Bindings::class])
class LocationDataModule {
    @Provides
    fun locationClient(application: Application): FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(application)

    @Module
    interface Bindings {
        @Binds
        fun locationDataSource(locationDataSourceImpl: LocationDataSourceImpl): LocationDataSource
    }
}
