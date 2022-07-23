package akio.apps.myrun.feature.tracking

import akio.apps.myrun.data.location.api.model.LocationRequestConfig
import android.app.Activity
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.LocationSettingsStatusCodes
import com.google.android.gms.location.Priority
import com.google.android.gms.location.SettingsClient
import kotlin.coroutines.resume
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

internal class LocationServiceChecker(
    private val activity: Activity,
    private val rcResolution: Int,
) {

    private val locationSettingsClient: SettingsClient =
        LocationServices.getSettingsClient(activity)

    private var resolutionContinuation: CancellableContinuation<Boolean>? = null

    suspend fun check(requestEntity: LocationRequestConfig): Boolean = withContext(Dispatchers.IO) {
        val sampleRequest = LocationRequest.create().apply {
            interval = requestEntity.updateInterval
            fastestInterval = requestEntity.fastestUpdateInterval
            priority = Priority.PRIORITY_HIGH_ACCURACY
            smallestDisplacement = requestEntity.smallestDisplacement
        }
        val settingsReq = LocationSettingsRequest.Builder()
            .addAllLocationRequests(listOf(sampleRequest))
            .build()

        return@withContext try {
            locationSettingsClient.checkLocationSettings(settingsReq).await()
            true
        } catch (apiEx: ApiException) {
            when (apiEx.statusCode) {
                LocationSettingsStatusCodes.RESOLUTION_REQUIRED -> {
                    (apiEx as? ResolvableApiException)?.let { resolveError(it) } ?: false
                }
                else -> false
            }
        }
    }

    private suspend fun resolveError(apiEx: ResolvableApiException): Boolean =
        suspendCancellableCoroutine { continuation ->
            resolutionContinuation = continuation
            apiEx.startResolutionForResult(
                activity,
                rcResolution
            )
        }

    fun verifyLocationServiceResolutionResult(resultCode: Int) {
        if (resultCode == Activity.RESULT_OK) {
            resolutionContinuation?.resume(true)
        } else {
            resolutionContinuation?.resume(false)
        }
    }
}
