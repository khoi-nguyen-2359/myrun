package akio.apps.myrun.data.location.impl

import akio.apps.myrun.data.location.LocationDataSource
import android.annotation.SuppressLint
import android.location.Location
import com.google.android.gms.location.FusedLocationProviderClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject

class LocationDataSourceImpl @Inject constructor(
    private val locationClient: FusedLocationProviderClient
): LocationDataSource {

    @SuppressLint("MissingPermission")
    override suspend fun getLastLocation(): Location = withContext(Dispatchers.IO) {
        locationClient.lastLocation.await()
    }
}