package akio.apps.myrun.data.location.impl

import akio.apps.myrun.data.location.LocationDataSource
import android.content.Context
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import dagger.Module
import dagger.Provides

@Module
class LocationDataModule {
    @Provides
    fun locationClient(appContext: Context): FusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(appContext)

    @Provides
    fun locationDataSource(locationDataSourceImpl: LocationDataSourceImpl): LocationDataSource = locationDataSourceImpl
}