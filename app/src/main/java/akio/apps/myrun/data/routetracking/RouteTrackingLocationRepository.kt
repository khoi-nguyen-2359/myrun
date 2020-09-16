package akio.apps.myrun.data.routetracking

import android.location.Location

interface RouteTrackingLocationRepository {
    fun insert(trackingLocations: List<Location>)
}