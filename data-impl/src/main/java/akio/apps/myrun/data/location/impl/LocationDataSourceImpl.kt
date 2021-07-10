package akio.apps.myrun.data.location.impl

import akio.apps.myrun.data.location.LocationDataSource
import akio.apps.myrun.data.location.LocationEntity
import akio.apps.myrun.data.routetracking.model.LocationRequestConfig
import android.annotation.SuppressLint
import android.location.Location
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import timber.log.Timber
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class LocationDataSourceImpl @Inject constructor(
    private val locationClient: FusedLocationProviderClient
) : LocationDataSource {

    override suspend fun getLastLocation(): LocationEntity? = withContext(Dispatchers.IO) {
        try {
            val androidLocation = locationClient.lastLocation.await()
            androidLocation?.toLocationEntity()
        } catch (ex: SecurityException) {
            null
        }
    }

    override fun getLastLocationFlow(): Flow<LocationEntity> = flow {
        val lastLocation = getLastLocation()
        if (lastLocation != null) {
            emit(lastLocation)
        } else {
            val request = LocationRequestConfig(0, 0, 0f)
            val result = getLocationUpdate(request).first { it.isNotEmpty() }
            emit(result.last())
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @SuppressLint("MissingPermission")
    override fun getLocationUpdate(request: LocationRequestConfig): Flow<List<LocationEntity>> =
        callbackFlow {
            Timber.d("=== [START] Get location update config=$request")
            getLastLocation()?.let { lastLocation ->
                trySend(listOf(lastLocation))
            }

            val callback = object : LocationCallback() {
                override fun onLocationResult(locationResult: LocationResult) {
                    trySend(locationResult.locations.map { it.toLocationEntity() })
                }
            }

            locationClient.requestLocationUpdates(
                request.toGmsLocationRequest(),
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

    private fun Location.toLocationEntity(): LocationEntity = LocationEntity(
        time, latitude, longitude, altitude, speed.toDouble()
    )
}
