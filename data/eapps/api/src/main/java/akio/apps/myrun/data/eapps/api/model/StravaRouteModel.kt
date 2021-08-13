package akio.apps.myrun.data.eapps.api.model

class StravaRouteModel(
    val map: StravaRouteMapModel?,
    val name: String,
    val segments: List<StravaSegmentModel>?
) {
    fun getEncodedPolyline() = map?.polyline ?: map?.summary_polyline
    fun getCountry() = segments?.firstOrNull()?.country
    fun getCity() = segments?.firstOrNull()?.city
}

class StravaRouteMapModel(
    val polyline: String?,
    val summary_polyline: String?
)

class StravaSegmentModel(
    val country: String?,
    val city: String?
)
