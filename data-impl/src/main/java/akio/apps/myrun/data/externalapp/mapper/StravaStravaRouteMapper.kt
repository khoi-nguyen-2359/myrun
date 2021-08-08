package akio.apps.myrun.data.externalapp.mapper

import akio.apps.myrun.data.externalapp.model.StravaRouteMapModel
import akio.apps.myrun.data.externalapp.model.StravaRouteModel
import akio.apps.myrun.data.externalapp.model.StravaSegmentModel
import akio.apps.myrun.data.externalapp.model.StravaStravaRoute
import javax.inject.Inject

class StravaStravaRouteMapper @Inject constructor() {
    fun map(strava: StravaStravaRoute) = strava.run {
        StravaRouteModel(
            map?.run { StravaRouteMapModel(polyline, summaryPolyline) },
            name,
            segments?.map { StravaSegmentModel(it.country, it.city) }
        )
    }
}
