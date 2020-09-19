package akio.apps.myrun.feature._base.utils

import akio.apps.myrun.R
import android.app.Activity
import androidx.appcompat.app.AlertDialog
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class CheckLocationServiceDelegate(
    private val activity: Activity,
    private val locationSampleRequests: List<LocationRequest>,
    private val rcLocationService: Int,
    private val onLocationServiceAvailable: () -> Any
) {

    private val locationSettingsClient: SettingsClient = LocationServices.getSettingsClient(activity)

    // work with activity only!!
    suspend fun checkLocationServiceAvailability(activity: Activity) = withContext(Dispatchers.IO) {
        val settingsReq = LocationSettingsRequest.Builder()
            .addAllLocationRequests(locationSampleRequests)
            .build()

        return@withContext try {
            locationSettingsClient.checkLocationSettings(settingsReq).await()
            withContext(Dispatchers.Main) { onLocationServiceAvailable() }
        } catch (apiEx: ApiException) {
            when (apiEx.statusCode) {
                LocationSettingsStatusCodes.RESOLUTION_REQUIRED -> {
                    (apiEx as? ResolvableApiException)?.startResolutionForResult(activity, rcLocationService)
                }

                else -> {
                    showLocationServiceUnavailableDialog()
                }
            }
        }
    }

    fun verifyLocationServiceResolutionResult(requestCode: Int, resultCode: Int) {
        if (requestCode == rcLocationService)
            if (resultCode == Activity.RESULT_OK)
                onLocationServiceAvailable()
            else
                showLocationServiceUnavailableDialog()
    }

    private fun showLocationServiceUnavailableDialog() {
        AlertDialog.Builder(activity)
            .setMessage(R.string.error_location_service_unavailable)
            .setPositiveButton(R.string.action_close) { _, _ ->
                activity.finish()
            }
            .setCancelable(false)
            .show()
    }
}