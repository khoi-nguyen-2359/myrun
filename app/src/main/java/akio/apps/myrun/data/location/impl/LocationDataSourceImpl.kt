package akio.apps.myrun.data.location.impl

import akio.apps.myrun.data.location.LocationDataSource
import akio.apps.myrun.data.location.model.LocationRequestEntity
import android.annotation.SuppressLint
import android.location.Location
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.sendBlocking
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

    @SuppressLint("MissingPermission")
    override suspend fun getLastLocation(): Location? = withContext(Dispatchers.IO) {
        locationClient.lastLocation.await()
    }

    @SuppressLint("MissingPermission")
    override fun getLocationUpdate(locationRequest: LocationRequestEntity): Flow<List<Location>> = callbackFlow<List<Location>> {

        val callback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                Timber.d("location result on thread ${Thread.currentThread().name}")
                sendBlocking(locationResult.locations)
            }
        }

        Timber.d("request location result on thread ${Thread.currentThread().name}")
        locationClient.requestLocationUpdates(locationRequest.toGmsLocationRequest(), callback, null)

        awaitClose {
            Timber.d("close location update flow")
            locationClient.removeLocationUpdates(callback)
        }
    }
        .flowOn(Dispatchers.Main)   // need Main to request updates from location client

    private fun LocationRequestEntity.toGmsLocationRequest(): LocationRequest {
        return LocationRequest().also {
            it.fastestInterval = fastestInterval
            it.interval = interval
            it.maxWaitTime = maxWaitTime
            it.priority = priority
            it.smallestDisplacement = smallestDisplacement
        }
    }
}