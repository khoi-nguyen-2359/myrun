package akio.apps.myrun.feature._base

import android.location.Location
import com.google.maps.android.SphericalUtil
import timber.log.Timber

object MapUtils {
    fun distanceBetweenTrackingLocations(locations: List<Location>): Double {
        Timber.d("distanceBetweenTrackingLocations ${locations.size}")
        var distance = 0.0
        for (i in 0..locations.size - 2) {
            distance += SphericalUtil.computeDistanceBetween(locations[i].toGmsLatLng(), locations[i + 1].toGmsLatLng())
        }
        return distance
    }
}