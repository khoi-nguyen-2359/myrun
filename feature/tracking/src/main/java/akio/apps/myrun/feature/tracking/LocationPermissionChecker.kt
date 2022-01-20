package akio.apps.myrun.feature.tracking

import akio.apps.myrun.feature.base.permissions.AppPermissions
import android.content.pm.PackageManager
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import kotlin.coroutines.resume
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.suspendCancellableCoroutine

class LocationPermissionChecker(
    private val activity: AppCompatActivity,
) {
    private val requestPermissionLauncher = activity.registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { resultMap ->
        val isGranted = resultMap[AppPermissions.preciseLocationPermission] == true
        continuation?.resume(isGranted)
    }

    private var continuation: CancellableContinuation<Boolean>? = null

    fun isGranted(): Boolean =
        ContextCompat.checkSelfPermission(activity, AppPermissions.preciseLocationPermission) ==
            PackageManager.PERMISSION_GRANTED

    suspend fun check(): Boolean = suspendCancellableCoroutine { continuation ->
        this.continuation = continuation
        when {
            ActivityCompat.shouldShowRequestPermissionRationale(
                activity,
                AppPermissions.preciseLocationPermission
            ) -> showRationaleAlert(activity)
            else -> requestLocationPermission()
        }
    }

    private fun showRationaleAlert(activity: AppCompatActivity) = AlertDialog.Builder(activity)
        .setMessage(R.string.location_permission_rationale_message)
        .setPositiveButton(R.string.action_proceed) { _, _ ->
            requestLocationPermission()
        }
        .setNegativeButton(R.string.action_cancel) { _, _ ->
            continuation?.resume(false)
        }
        .show()

    private fun requestLocationPermission() {
        requestPermissionLauncher.launch(AppPermissions.locationPermissions)
    }
}
