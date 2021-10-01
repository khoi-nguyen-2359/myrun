package akio.apps.myrun.feature.googlefit

import akio.apps.myrun._base.permissions.RequiredPermissionsDelegate
import akio.apps.myrun.feature.base.permissions.PermissionUtils
import android.Manifest
import android.app.Activity
import android.os.Build
import androidx.fragment.app.Fragment
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import kotlinx.coroutines.Job
import kotlinx.coroutines.tasks.await

class GoogleFitLinkingDelegate {
    private val fitnessDataPermissionJob = Job()
    private val activityRegPermissionJob = Job()

    /**
     * @return false to indicate there's an error while connecting
     */
    suspend fun requestGoogleFitPermissions(
        activity: Activity,
        rcActivityRecognitionPermission: Int,
        rcFitnessDataPermission: Int,
        fragment: Fragment? = null,
    ) {
        if (isGoogleFitLinked(activity)) {
            return
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            RequiredPermissionsDelegate.requestPermissions(
                arrayOf(Manifest.permission.ACTIVITY_RECOGNITION),
                rcActivityRecognitionPermission,
                activity,
                fragment
            )
            activityRegPermissionJob.join()
        }

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q || PermissionUtils.arePermissionsGranted(
                activity,
                arrayOf(Manifest.permission.ACTIVITY_RECOGNITION)
            )
        ) {
            if (RequiredPermissionsDelegate.requestFitnessDataPermissions(
                    activity,
                    rcFitnessDataPermission,
                    fragment
                )
            ) {
                fitnessDataPermissionJob.join()
            }
        }
    }

    suspend fun disconnectGoogleFit(activity: Activity): Boolean {
        try {
            GoogleSignIn.getClient(
                activity,
                GoogleSignInOptions.Builder()
                    .addExtension(RequiredPermissionsDelegate.buildFitnessDataOptions())
                    .build()
            )
                .revokeAccess()
                .await()
        } catch (ex: Exception) {
            return false
        }

        return true
    }

    fun verifyActivityRecognitionPermission() {
        activityRegPermissionJob.complete()
    }

    fun verifyFitnessDataPermission() {
        fitnessDataPermissionJob.complete()
    }

    fun isGoogleFitLinked(activity: Activity): Boolean {
        return RequiredPermissionsDelegate.isFitnessDataPermissionsGranted(activity)
    }
}
