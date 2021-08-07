package akio.apps.myrun._base.permissions

import android.Manifest
import com.google.android.gms.fitness.data.DataType

object AppPermissions {
    const val preciseLocationPermission: String = Manifest.permission.ACCESS_FINE_LOCATION
    const val approximateLocationPermission: String = Manifest.permission.ACCESS_COARSE_LOCATION

    // From Android 12, location permission always include this couple:
    val locationPermissions = arrayOf(preciseLocationPermission, approximateLocationPermission)
    val takePhotoPermissions = arrayOf(
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.CAMERA
    )
    val pickPhotoPermissions = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
    val appLaunchPermissions =
        arrayOf(*locationPermissions, *takePhotoPermissions, *pickPhotoPermissions)
    val fitnessDataTypes = arrayOf(
        DataType.TYPE_SPEED,
        DataType.TYPE_STEP_COUNT_DELTA,
        DataType.TYPE_STEP_COUNT_CADENCE,
        DataType.TYPE_STEP_COUNT_CUMULATIVE
    )
}
