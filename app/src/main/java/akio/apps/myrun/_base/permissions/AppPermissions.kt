package akio.apps.myrun._base.permissions

import android.Manifest
import com.google.android.gms.fitness.data.DataType

object AppPermissions {
    val locationPermissions = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
    val takePhotoPermissions = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA)
    val pickPhotoPermissions = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
    val appLaunchPermissions = arrayOf(*locationPermissions, *takePhotoPermissions, *pickPhotoPermissions)
    val fitnessDataPermissions = arrayOf(
        DataType.TYPE_CALORIES_EXPENDED,
        DataType.AGGREGATE_CALORIES_EXPENDED,
        DataType.TYPE_STEP_COUNT_CADENCE,
        DataType.TYPE_SPEED,
        DataType.AGGREGATE_SPEED_SUMMARY,
        DataType.TYPE_STEP_COUNT_DELTA,
        DataType.AGGREGATE_STEP_COUNT_DELTA,
        DataType.TYPE_HEART_RATE_BPM
    )
}