package akio.apps.myrun.feature.core.permissions

import android.Manifest
import android.os.Build

object AppPermissions {
    const val preciseLocationPermission: String = Manifest.permission.ACCESS_FINE_LOCATION
    private const val approximateLocationPermission: String =
        Manifest.permission.ACCESS_COARSE_LOCATION

    // From Android 12, location permission always include this couple:
    val locationPermissions = arrayOf(preciseLocationPermission, approximateLocationPermission)
    val takePhotoPermissions = arrayOf(
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.CAMERA
    )
    val pickPhotoPermissions = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE) +
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arrayOf(Manifest.permission.READ_MEDIA_IMAGES)
        } else {
            emptyArray()
        }
}
