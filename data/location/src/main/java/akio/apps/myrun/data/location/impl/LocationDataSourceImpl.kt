package akio.apps.myrun.data.location.impl

import akio.apps.myrun.data.location.api.LOG_TAG_LOCATION
import akio.apps.myrun.data.location.api.LocationDataSource
import akio.apps.myrun.data.location.api.model.Location
import akio.apps.myrun.data.location.api.model.LocationRequestConfig
import akio.apps.myrun.data.location.di.LocationDataScope
import android.annotation.SuppressLint
import android.os.Looper
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.Priority
import com.squareup.anvil.annotations.ContributesBinding
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.tasks.await
import timber.log.Timber

private typealias AndroidLocation = android.location.Location

@Singleton
@ContributesBinding(LocationDataScope::class)
class LocationDataSourceImpl @Inject constructor(
    private val locationClient: FusedLocationProviderClient,
) : LocationDataSource {

    @SuppressLint("MissingPermission")
    override suspend fun getLastLocation(): Location? = try {
        val androidLocation = locationClient.lastLocation.await()
        androidLocation?.toLocation()
    } catch (ex: SecurityException) {
        null
    }

    override fun getLastLocationFlow(): Flow<Location> = flow {
        val lastLocation = getLastLocation()
        if (lastLocation != null) {
            emit(lastLocation)
        } else {
            val request = LocationRequestConfig()
            val result = getLocationUpdate(request).first { it.isNotEmpty() }
            emit(result.last())
        }
    }

    @SuppressLint("MissingPermission")
    override fun getLocationUpdate(request: LocationRequestConfig): Flow<List<Location>> =
        callbackFlow {
            Timber.d("=== [START] Get location update, config=$request")
            val callback = object : LocationCallback() {
                override fun onLocationResult(locationResult: LocationResult) {
                    Timber.tag(LOG_TAG_LOCATION).d(
                        "[LocationDataSourceImpl] location Result: ${locationResult.locations.size}"
                    )
                    trySend(locationResult.locations.map { it.toLocation() })
                }
            }

            locationClient.requestLocationUpdates(
                request.toGmsLocationRequest(),
                callback,
                Looper.getMainLooper()
            )

            awaitClose {
                Timber.d("=== [STOP] close location update flow")
                locationClient.removeLocationUpdates(callback)
            }
        }
            .flowOn(Dispatchers.Main) // need Main to request updates from location client

    private fun LocationRequestConfig.toGmsLocationRequest(): LocationRequest =
        LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, updateInterval)
            .setMinUpdateIntervalMillis(minUpdateInterval)
            .setMinUpdateDistanceMeters(minUpdateDistance)
            .setMaxUpdateDelayMillis(maxUpdateInterval)
            .build()

    private fun AndroidLocation.toLocation(): Location =
        Location(
            elapsedRealtimeNanos / 1000000,
            System.currentTimeMillis(),
            latitude,
            longitude,
            altitude,
            speed.toDouble()
        )
}
