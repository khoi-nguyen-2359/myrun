package akio.apps.myrun._base.utils

import akio.apps.myrun.R
import android.app.Activity
import androidx.appcompat.app.AlertDialog
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class CheckLocationServiceDelegate(
    activity: Activity,
    private val locationSampleRequests: List<LocationRequest>
) {

    private val locationSettingsClient: SettingsClient = LocationServices.getSettingsClient(activity)
    private val resolveForResultJob = Job()
    private val showLocationUnavailableDialogJob = Job()

    // work with activity only!!
    suspend fun checkLocationServiceAvailability(activity: Activity, rcLocationService: Int): Boolean = withContext(Dispatchers.IO) {
        val settingsReq = LocationSettingsRequest.Builder()
            .addAllLocationRequests(locationSampleRequests)
            .build()

        return@withContext try {
            locationSettingsClient.checkLocationSettings(settingsReq).await()
            true
        } catch (apiEx: ApiException) {
            when (apiEx.statusCode) {
                LocationSettingsStatusCodes.RESOLUTION_REQUIRED -> {
                    (apiEx as? ResolvableApiException)?.startResolutionForResult(activity, rcLocationService)
                    try {
                        resolveForResultJob.join()
                        true
                    } catch (ex: ResolutionNotFound) {
                        showLocationServiceUnavailableDialog(activity)
                    }
                }

                else -> {
                    showLocationServiceUnavailableDialog(activity)
                }
            }
        }
    }

    fun verifyLocationServiceResolutionResult(resultCode: Int) {
        if (resultCode == Activity.RESULT_OK) {
            resolveForResultJob.complete()
        } else {
            resolveForResultJob.completeExceptionally(ResolutionNotFound())
        }
    }

    private suspend fun showLocationServiceUnavailableDialog(activity: Activity): Boolean {
        AlertDialog.Builder(activity)
            .setMessage(R.string.error_location_service_unavailable)
            .setPositiveButton(R.string.action_close) { _, _ ->
                showLocationUnavailableDialogJob.complete()
            }
            .setCancelable(false)
            .show()
        showLocationUnavailableDialogJob.join()
        return false
    }

    internal class ResolutionNotFound: Throwable()
}