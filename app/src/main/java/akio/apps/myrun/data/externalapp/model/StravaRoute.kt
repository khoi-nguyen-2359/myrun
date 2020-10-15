package akio.apps.myrun.data.externalapp.model

class StravaRoute(
    val map: StravaRouteMap?,
    val name: String,
    val segments: List<StravaSegment>?
) {
    fun getEncodedPolyline() = map?.polyline ?: map?.summary_polyline
    fun getCountry() = segments?.firstOrNull()?.country
    fun getCity() = segments?.firstOrNull()?.city
}

class StravaRouteMap(
    val polyline: String?,
    val summary_polyline: String?
)

class StravaSegment(
    val country: String?,
    val city: String?
)