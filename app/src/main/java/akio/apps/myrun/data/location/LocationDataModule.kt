package akio.apps.myrun.data.location

import akio.apps.myrun.data.location.impl.LocationDataSourceImpl
import android.app.Activity
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