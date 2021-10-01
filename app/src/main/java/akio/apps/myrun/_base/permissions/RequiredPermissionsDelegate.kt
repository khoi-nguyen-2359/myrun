package akio.apps.myrun._base.permissions

import akio.apps._base.utils.GoogleSignInPermissionUtils
import akio.apps.myrun.R
import akio.apps.myrun.feature.base.permissions.PermissionUtils
import android.app.Activity
import android.content.Context
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.google.android.gms.fitness.FitnessOptions
import com.google.android.gms.fitness.data.DataType
import kotlinx.coroutines.Job

class RequiredPermissionsDelegate {

    private val requestPermissionJob = Job()

    suspend fun requestPermissions(
        permissions: Array<String>,
        rcPermissions: Int,
        activity: Activity,
        fragment: Fragment? = null,
    ): Boolean {
        RequiredPermissionsDelegate.requestPermissions(
            permissions,
            rcPermissions,
            activity,
            fragment
        )
        return try {
            requestPermissionJob.join()
            true
        } catch (ex: PermissionRequestError) {
            false
        }
    }

    fun verifyPermissionsResult(context: Context, permissions: Array<String>) {
        if (PermissionUtils.arePermissionsGranted(context, permissions)) {
            requestPermissionJob.complete()
            return
        }

        AlertDialog.Builder(context)
            .setMessage(R.string.error_required_permissions_missing)
            .setPositiveButton(R.string.action_close) { _, _ ->
                requestPermissionJob.completeExceptionally(PermissionRequestError())
            }
            .setCancelable(false)
            .show()
    }

    companion object {
        private val fitnessDataTypes = arrayOf(
            DataType.TYPE_SPEED,
            DataType.TYPE_STEP_COUNT_DELTA,
            DataType.TYPE_STEP_COUNT_CADENCE,
            DataType.TYPE_STEP_COUNT_CUMULATIVE
        )

        fun requestPermissions(
            permissions: Array<String>,
            rcPermissions: Int,
            activity: Activity,
            fragment: Fragment? = null,
        ) {
            if (fragment != null) {
                @Suppress("DEPRECATION")
                fragment.requestPermissions(permissions, rcPermissions)
            } else {
                ActivityCompat.requestPermissions(activity, permissions, rcPermissions)
            }
        }

        fun requestFitnessDataPermissions(
            activity: Activity,
            requestCode: Int,
            fragment: Fragment? = null,
        ): Boolean {
            return GoogleSignInPermissionUtils.mayRequestGoogleSignInPermissions(
                activity,
                fitnessDataTypes,
                requestCode,
                fragment
            )
        }

        fun isFitnessDataPermissionsGranted(activity: Activity): Boolean {
            return GoogleSignInPermissionUtils.isDataTypePermissionsGranted(
                activity,
                fitnessDataTypes
            )
        }

        fun buildFitnessDataOptions(): FitnessOptions {
            return GoogleSignInPermissionUtils.buildFitnessOptions(fitnessDataTypes)
        }
    }

    internal class PermissionRequestError : Throwable()
}
