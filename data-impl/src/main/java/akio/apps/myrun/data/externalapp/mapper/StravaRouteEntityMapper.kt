package akio.apps.myrun.data.externalapp.mapper

import akio.apps.myrun.data.externalapp.entity.StravaRouteEntity
import akio.apps.myrun.data.externalapp.model.StravaRoute
import akio.apps.myrun.data.externalapp.model.StravaRouteMap
import akio.apps.myrun.data.externalapp.model.StravaSegment
import javax.inject.Inject

class StravaRouteEntityMapper @Inject constructor() {
    fun map(entity: StravaRouteEntity) = entity.run {
        StravaRoute(
            map?.run { StravaRouteMap(polyline, summaryPolyline) },
            name,
            segments?.map { StravaSegment(it.country, it.city) }
        )
    }
}
