package akio.apps.myrun.data.eapps.impl.mapper

import akio.apps.myrun.data.eapps.api.model.StravaRouteMapModel
import akio.apps.myrun.data.eapps.api.model.StravaRouteModel
import akio.apps.myrun.data.eapps.api.model.StravaSegmentModel
import akio.apps.myrun.data.eapps.impl.model.StravaStravaRoute
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
