package akio.apps.myrun.data.location.impl

import akio.apps.myrun.data.location.LocationDataSource
import akio.apps.myrun.data.routetracking.model.LocationRequestConfig
import android.annotation.SuppressLint
import android.location.Location
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject

class LocationDataSourceImpl @Inject constructor(
    private val locationClient: FusedLocationProviderClient
) : LocationDataSource {

    override suspend fun getLastLocation(): Location? = withContext(Dispatchers.IO) {
        try {
            locationClient.lastLocation.await()
        } catch (ex: SecurityException) {
            null
        }
    }

    @ExperimentalCoroutinesApi
    @SuppressLint("MissingPermission")
    override fun getLocationUpdate(locationRequest: LocationRequestConfig): Flow<List<Location>> =
        callbackFlow<List<Location>> {
            Timber.d("=== [START] Get location update config=$locationRequest")
            getLastLocation()?.let { lastLocation ->
                trySend(listOf(lastLocation))
            }

            val callback = object : LocationCallback() {
                override fun onLocationResult(locationResult: LocationResult) {
                    trySend(locationResult.locations)
                }
            }

            locationClient.requestLocationUpdates(
                locationRequest.toGmsLocationRequest(),
                callback,
                null
            )

            awaitClose {
                Timber.d("=== [STOP] close location update flow")
                locationClient.removeLocationUpdates(callback)
            }
        }
            .flowOn(Dispatchers.Main) // need Main to request updates from location client

    private fun LocationRequestConfig.toGmsLocationRequest(): LocationRequest {
        val locationRequest = LocationRequest.create()
        locationRequest.fastestInterval = fastestUpdateInterval
        locationRequest.interval = updateInterval
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        locationRequest.smallestDisplacement = smallestDisplacement
        return locationRequest
    }
}
